package com.example.demo.domain.user.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class LoginResponseDto {

    @Getter
    @AllArgsConstructor
    public static class Oauth2Result {
        private final boolean isNewUser;
        private final String tempCode;
    }

    @Getter
    @AllArgsConstructor
    public static class LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final LocalDateTime accessTokenExpiredAt; // 엑세스 토큰 만료 시간
    }

}