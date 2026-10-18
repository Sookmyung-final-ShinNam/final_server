package com.example.demo.domain.user.service.command;

/**
 * 이메일 인증 관련 Command 서비스
 * 이메일 인증코드 발송 및 검증
 */
public interface EmailVerificationCommandService {

    // 이메일 인증코드 발송
    public void sendCode(String tempCode, String email);

    // 이메일 인증코드 검증
    public void verifyCode(String tempCode, String code);
}
