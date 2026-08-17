package com.example.demo.domain.user.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.EmailVerification;
import com.example.demo.domain.user.entity.Token;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.EmailVerificationRepository;
import com.example.demo.domain.user.repository.TokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증 관련 Command 서비스
 * 이메일 인증코드 발송 및 검증
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationCommandServiceImpl implements EmailVerificationCommandService {

    private final TokenRepository tokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender javaMailSender;

    // 이메일 인증코드 발송
    @Override
    public void sendCode(String tempCode, String email) {

        // 이미 다른 계정에서 인증된 이메일인 경우
        emailVerificationRepository.findByEmail(email)
                .filter(EmailVerification::isVerified)
                .ifPresent(ev -> {
                    throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_ALREADY_COMPLETED);
                });


        // 본인 기존 인증 시도 삭제 (재요청/이메일 변경 시 이전 것 무효화)
        User user = getUserByTempCode(tempCode);
        emailVerificationRepository.deleteAllByUser(user);

        // 6자리 랜덤 숫자
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .code(code)
                .user(user)
                .build();
        emailVerificationRepository.save(emailVerification);

        sendMail(email,code);
    }

    // 이메일 인증코드 검증
    @Override
    public void verifyCode(String tempCode, String code) {

        // 인증 내역 조회
        User user = getUserByTempCode(tempCode);
        EmailVerification emailVerification = emailVerificationRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorStatus.EMAIL_VERIFICATION_NOT_FOUND));

        // 인증코드 유효시간 확인
        if (emailVerification.isExpired()) {
            throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        // 인증코드 일치 확인
        if (!emailVerification.isEqual(code)) {
            throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        emailVerification.verify();
    }

    // 임시 토큰으로 사용자 조회
    private User getUserByTempCode(String tempCode) {
        Token token = tokenRepository.findByTempCode(tempCode)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        return token.getUser();
    }

    // 메일 코드 발송
    private void sendMail(String email, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            String html = """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #F3A600;">Storictor 이메일 인증</h2>
                    <p>아래 인증코드를 확인하신 후,<br>
                    Storictor 앱으로 돌아가 이메일 인증을 완료해주세요.</p>
                    <div style="font-size: 28px; font-weight: bold; letter-spacing: 6px;
                                background: #FEF6E4; padding: 20px; text-align: center; border-radius: 8px;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 13px; margin-top: 16px;">
                        해당 코드는 5분간 유효합니다.
                    </p>
                </div>
                """.formatted(code);

            helper.setTo(email);
            helper.setSubject("[Storictor] 이메일 인증코드");
            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new CustomException(ErrorStatus.EMAIL_SEND_FAILED);
        }
    }
}
