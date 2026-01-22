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
        log.info("\n" +
                "================================================\n" +
                "[MOCK MESSAGE SERVER]\n" +
                "수신번호: {}\n" +
                "인증번호: {}\n" +
                "발송 결과: 실제 발송 대신 로그로 대체되었습니다.\n" +
                "================================================", phone, code);
    }
}
