package com.example.demo.domain.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class EmailVerificationRequest {

    @Getter
    @NoArgsConstructor
    public static class Send {
        @NotBlank(message = "임시 토큰은 필수 입니다.")
        private String tempCode;

        @NotBlank(message = "이메일은 필수 입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    public static class Verification {
        @NotBlank(message = "임시 토큰은 필수 입니다.")
        private String tempCode;

        @NotBlank(message = "인증 코드는 필수 입니다.")
        private String code;
    }
}
