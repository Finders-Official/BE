package com.finders.api.infra.messaging;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import io.sendon.Sendon;
import io.sendon.kakao.request.AlimtalkBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile({"dev", "prod"})
public class SendonMessageService implements MessageService {

    private static final String VAR_SERVICE_NAME = "#{서비스명}";
    private static final String VAR_VERIFICATION_CODE = "#{인증번호}";

    private final Sendon sendon;
    private final String serviceName;
    private final String senderKey;
    private final String phoneTemplateCode;

    public SendonMessageService(
            Sendon sendon,
            @Value("${sendon.service-name}") String serviceName,
            @Value("${sendon.sender-key}") String senderKey,
            @Value("${sendon.template.phone-code}") String phoneTemplateCode
    ) {
        this.sendon = sendon;
        this.serviceName = serviceName;
        this.senderKey = senderKey;
        this.phoneTemplateCode = phoneTemplateCode;
    }

    @Override
    public void sendVerificationCode(String phone, String code) {
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", "");

            Map<String, String> variables = Map.of(
                    VAR_SERVICE_NAME, serviceName,
                    VAR_VERIFICATION_CODE, code
            );

            Map<String, Object> receiver = Map.of(
                    "phone", cleanPhone,
                    "variables", variables
            );

            sendon.kakao.sendAlimtalk(new AlimtalkBuilder()
                    .setProfileId(senderKey)
                    .setTemplateId(phoneTemplateCode)
                    .setTo(List.of(receiver))
            );

            log.info("[SendonMessageService.sendVerificationCode] 알림톡 발송 성공 - 대상: {}", cleanPhone);
        } catch (Exception e) {
            log.error("[SendonMessageService.sendVerificationCode] 알림톡 발송 실패 - 대상: {}", phone, e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
