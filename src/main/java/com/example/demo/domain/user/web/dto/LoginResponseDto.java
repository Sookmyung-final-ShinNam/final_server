package com.example.demo.domain.user.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class LoginResponseDto {

    @Getter
    @AllArgsConstructor
    public static class Oauth2Result {
        private final boolean isNewUser;
        private final String tempCode;
    }

}