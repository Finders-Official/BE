package com.finders.api.infra.messaging;

public interface MessageService {
    void sendVerificationCode(String phone, String code);
}
