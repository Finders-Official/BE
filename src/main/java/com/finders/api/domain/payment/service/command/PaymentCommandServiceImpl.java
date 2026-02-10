package com.finders.api.domain.payment.service.command;

import com.finders.api.domain.member.entity.Member;
import com.finders.api.domain.member.entity.MemberUser;
import com.finders.api.domain.member.repository.MemberRepository;
import com.finders.api.domain.member.service.command.CreditCommandService;
import com.finders.api.domain.payment.dto.PaymentRequest;
import com.finders.api.domain.payment.dto.PaymentResponse;
import com.finders.api.domain.payment.entity.Payment;
import com.finders.api.domain.payment.enums.OrderType;
import com.finders.api.domain.payment.enums.PaymentStatus;
import com.finders.api.domain.payment.repository.PaymentRepository;
import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import com.finders.api.infra.payment.PortOneException;
import com.finders.api.infra.payment.PortOnePaymentInfo;
import com.finders.api.infra.payment.PortOnePaymentService;
import com.finders.api.infra.payment.PortOneWebhookInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PortOnePaymentService portOnePaymentService;
    private final CreditCommandService creditCommandService;

    @Override
    public PaymentResponse.PreRegistered preRegister(Long memberId, PaymentRequest.PreRegister request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 중복 결제 ID 체크
        if (paymentRepository.existsByPaymentId(request.paymentId())) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 크레딧 구매 시 검증
        if (request.orderType() == OrderType.CREDIT_PURCHASE) {
            if (request.creditAmount() == null || request.creditAmount() < 1) {
                throw new CustomException(ErrorCode.INVALID_PAYMENT_REQUEST);
            }
        } else {
            // 주문 결제 시 relatedOrderId 필수
            if (request.relatedOrderId() == null) {
                throw new CustomException(ErrorCode.INVALID_PAYMENT_REQUEST);
            }
        }

        Payment payment = Payment.builder()
                .member(member)
                .orderType(request.orderType())
                .relatedOrderId(request.relatedOrderId())
                .paymentId(request.paymentId())
                .orderName(request.orderName())
                .amount(request.amount())
                .creditAmount(request.creditAmount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("[PaymentCommandServiceImpl.preRegister] 결제 사전등록 완료: paymentId={}, memberId={}, amount={}",
                request.paymentId(), memberId, request.amount());

        return PaymentResponse.PreRegistered.builder()
                .id(savedPayment.getId())
                .paymentId(savedPayment.getPaymentId())
                .orderName(savedPayment.getOrderName())
                .amount(savedPayment.getAmount())
                .status(savedPayment.getStatus())
                .build();
    }

    @Override
    public PaymentResponse.Detail complete(Long memberId, PaymentRequest.Complete request) {
        Payment payment = paymentRepository.findByPaymentId(request.paymentId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 확인
        if (!payment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 이미 처리된 결제인지 확인
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        // 포트원에서 결제 정보 조회
        PortOnePaymentInfo portOneInfo = portOnePaymentService.getPayment(request.paymentId());

        // 결제 상태에 따른 처리
        switch (portOneInfo.getStatus()) {
            case PAID -> {
                // 금액 검증
                if (!payment.getAmount().equals(portOneInfo.getAmount())) {
                    log.error("[PaymentCommandServiceImpl.complete] 결제 금액 불일치: expected={}, actual={}",
                            payment.getAmount(), portOneInfo.getAmount());
                    throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
                }

                payment.complete(
                        portOneInfo.getTransactionId(),
                        portOneInfo.getPgTxId(),
                        portOneInfo.getPgProvider(),
                        portOneInfo.getMethod(),
                        portOneInfo.getCardCompany(),
                        portOneInfo.getCardNumber(),
                        portOneInfo.getApproveNo(),
                        portOneInfo.getInstallmentMonths(),
                        portOneInfo.getReceiptUrl()
                );

                // 크레딧 구매인 경우 크레딧 충전
                if (payment.getOrderType() == OrderType.CREDIT_PURCHASE && payment.getCreditAmount() != null) {
                    chargeCredits(payment);
                }

                log.info("[PaymentCommandServiceImpl.complete] 결제 완료: paymentId={}, transactionId={}",
                        payment.getPaymentId(), portOneInfo.getTransactionId());
            }
            case VIRTUAL_ACCOUNT_ISSUED -> {
                payment.issueVirtualAccount(
                        portOneInfo.getTransactionId(),
                        portOneInfo.getPgTxId(),
                        portOneInfo.getPgProvider()
                );
                log.info("[PaymentCommandServiceImpl.complete] 가상계좌 발급: paymentId={}", payment.getPaymentId());
            }
            case FAILED -> {
                payment.fail(portOneInfo.getFailCode(), portOneInfo.getFailMessage());
                log.warn("[PaymentCommandServiceImpl.complete] 결제 실패: paymentId={}, reason={}",
                        payment.getPaymentId(), portOneInfo.getFailMessage());
            }
            default -> {
                log.warn("[PaymentCommandServiceImpl.complete] 처리할 수 없는 결제 상태: paymentId={}, status={}",
                        payment.getPaymentId(), portOneInfo.getStatus());
                throw new CustomException(ErrorCode.PAYMENT_INVALID_STATUS);
            }
        }

        return PaymentResponse.Detail.from(payment);
    }

    @Override
    public PaymentResponse.Cancelled cancel(Long memberId, String paymentId, PaymentRequest.Cancel request) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 본인 결제인지 확인
        if (!payment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 취소 가능 상태인지 확인
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
        }

        // 부분 취소 금액 검증
        if (request.amount() != null && request.amount() > payment.getAmount()) {
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_AMOUNT_EXCEEDED);
        }

        try {
            // 포트원에 취소 요청
            portOnePaymentService.cancelPayment(paymentId, request.amount(), request.reason());

            // 취소 금액 (null이면 전액)
            Integer cancelAmount = request.amount() != null ? request.amount() : payment.getAmount();

            payment.cancel(request.reason(), cancelAmount);

            // 크레딧 구매 취소인 경우 크레딧 회수 (전액 취소만)
            if (payment.getOrderType() == OrderType.CREDIT_PURCHASE
                    && payment.getStatus() == PaymentStatus.CANCELLED
                    && payment.getCreditAmount() != null) {
                revokeCredits(payment);
            }

            log.info("[PaymentCommandServiceImpl.cancel] 결제 취소 완료: paymentId={}, cancelAmount={}",
                    paymentId, cancelAmount);

            return PaymentResponse.Cancelled.builder()
                    .id(payment.getId())
                    .paymentId(payment.getPaymentId())
                    .status(payment.getStatus())
                    .cancelAmount(payment.getCancelAmount())
                    .cancelReason(payment.getCancelReason())
                    .cancelledAt(payment.getCancelledAt())
                    .build();

        } catch (PortOneException e) {
            log.error("[PaymentCommandServiceImpl.cancel] 포트원 결제 취소 실패: paymentId={}", paymentId, e);
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    @Override
    public void handleWebhook(String body, String webhookId, String webhookTimestamp, String webhookSignature) {
        try {
            PortOneWebhookInfo webhookInfo = portOnePaymentService.verifyWebhook(
                    body, webhookId, webhookTimestamp, webhookSignature);

            if (webhookInfo == null) {
                log.debug("처리할 필요 없는 웹훅: webhookId={}", webhookId);
                return;
            }

            Payment payment = paymentRepository.findByPaymentId(webhookInfo.getPaymentId())
                    .orElse(null);

            if (payment == null) {
                log.warn("[PaymentCommandServiceImpl.handleWebhook] 웹훅 대상 결제 없음: paymentId={}", webhookInfo.getPaymentId());
                return;
            }

            // 포트원에서 최신 결제 정보 조회
            PortOnePaymentInfo portOneInfo = portOnePaymentService.getPayment(webhookInfo.getPaymentId());

            // 이미 처리된 상태면 스킵
            if (payment.getStatus() == portOneInfo.getStatus()) {
                log.debug("이미 처리된 결제 상태: paymentId={}, status={}",
                        payment.getPaymentId(), payment.getStatus());
                return;
            }

            // 상태 업데이트
            switch (portOneInfo.getStatus()) {
                case PAID -> {
                    if (payment.getStatus() == PaymentStatus.VIRTUAL_ACCOUNT_ISSUED
                            || payment.getStatus() == PaymentStatus.READY) {
                        payment.complete(
                                portOneInfo.getTransactionId(),
                                portOneInfo.getPgTxId(),
                                portOneInfo.getPgProvider(),
                                portOneInfo.getMethod(),
                                portOneInfo.getCardCompany(),
                                portOneInfo.getCardNumber(),
                                portOneInfo.getApproveNo(),
                                portOneInfo.getInstallmentMonths(),
                                portOneInfo.getReceiptUrl()
                        );

                        // 크레딧 충전
                        if (payment.getOrderType() == OrderType.CREDIT_PURCHASE && payment.getCreditAmount() != null) {
                            chargeCredits(payment);
                        }

                        log.info("[PaymentCommandServiceImpl.handleWebhook] 웹훅으로 결제 완료 처리: paymentId={}", payment.getPaymentId());
                    }
                }
                case CANCELLED, PARTIAL_CANCELLED -> {
                    payment.updateStatus(portOneInfo.getStatus());
                    log.info("[PaymentCommandServiceImpl.handleWebhook] 웹훅으로 결제 취소 처리: paymentId={}, status={}",
                            payment.getPaymentId(), portOneInfo.getStatus());
                }
                case FAILED -> {
                    payment.fail(portOneInfo.getFailCode(), portOneInfo.getFailMessage());
                    log.info("[PaymentCommandServiceImpl.handleWebhook] 웹훅으로 결제 실패 처리: paymentId={}", payment.getPaymentId());
                }
                default -> log.debug("[PaymentCommandServiceImpl.handleWebhook] 웹훅 처리 스킵: paymentId={}, status={}",
                        payment.getPaymentId(), portOneInfo.getStatus());
            }

        } catch (PortOneException e) {
            log.error("[PaymentCommandServiceImpl.handleWebhook] 웹훅 처리 실패", e);
            throw new CustomException(ErrorCode.WEBHOOK_VERIFICATION_FAILED);
        }
    }

    private void chargeCredits(Payment payment) {
        Member member = payment.getMember();
        if (member instanceof MemberUser memberUser) {
            creditCommandService.purchaseCredits(memberUser, payment.getCreditAmount(), payment.getId());
            log.info("[PaymentCommandServiceImpl.chargeCredits] 크레딧 충전 완료: memberId={}, amount={}",
                    member.getId(), payment.getCreditAmount());
        }
    }

    private void revokeCredits(Payment payment) {
        Member member = payment.getMember();
        if (member instanceof MemberUser memberUser) {
            creditCommandService.revokeCredits(memberUser, payment.getCreditAmount(), payment.getId());
            log.info("[PaymentCommandServiceImpl.revokeCredits] 크레딧 회수 완료: memberId={}, amount={}",
                    member.getId(), payment.getCreditAmount());
        }
    }
}
