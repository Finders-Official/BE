package com.finders.api.infra.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("local")
public class ConsoleMessageService implements MessageService {
    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("[ConsoleMessageService.sendVerificationCode] MOCK - 수신번호: {}, 인증번호: {}", phone, code);
    }
}
