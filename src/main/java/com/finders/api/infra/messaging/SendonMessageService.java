package com.finders.api.infra.messaging;

import com.finders.api.global.exception.CustomException;
import com.finders.api.global.response.ErrorCode;
import io.sendon.Sendon;
import io.sendon.kakao.request.AlimtalkBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class SendonMessageService implements MessageService {
    private final Sendon sendon;

    @Value("${sendon.service-name}")
    private String serviceName;
    @Value("${sendon.sender-key}")
    private String senderKey;
    @Value("${sendon.template.phone-code}")
    private String phoneTemplateCode;

    @Override
    public void sendVerificationCode(String phone, String code) {
        try {
            Map<String, Object> receiver = new HashMap<>();
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            receiver.put("phone", cleanPhone);

            Map<String, String> variables = new HashMap<>();
            variables.put("#{서비스명}", serviceName);
            variables.put("#{인증번호}", code);
            receiver.put("variables", variables);

            sendon.kakao.sendAlimtalk(new AlimtalkBuilder()
                    .setProfileId(senderKey)
                    .setTemplateId(phoneTemplateCode)
                    .setTo(Arrays.asList(receiver))
            );

            log.info("[SendonMessageService.sendVerificationCode] 알림톡 발송 성공 - 대상: {}", cleanPhone);
        } catch (Exception e) {
            log.error("[SendonMessageService.sendVerificationCode] 알림톡 발송 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
