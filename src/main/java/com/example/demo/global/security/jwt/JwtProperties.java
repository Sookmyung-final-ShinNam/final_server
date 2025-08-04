package com.example.demo.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.jwt")
public class JwtProperties {

    private String secret;
    private TokenValidity tokenValidity;

    @Getter
    @Setter
    public static class TokenValidity {
        private long accessToken;
        private long refreshToken;
    }

}