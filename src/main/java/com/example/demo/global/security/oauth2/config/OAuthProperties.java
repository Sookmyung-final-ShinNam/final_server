package com.example.demo.global.security.oauth2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthProperties {

    private String kakaoPathUri;
    private Registration registration;
    private Provider provider;

    @Getter
    @Setter
    public static class Registration {
        private Kakao kakao;

        @Getter
        @Setter
        public static class Kakao {
            private String clientId;
            private String clientSecret;
            private String authorizationGrantType;
            private String redirectUri;
            private String[] scope;
            private String clientName;
            private String clientAuthenticationMethod;
        }
    }

    @Getter
    @Setter
    public static class Provider {
        private Kakao kakao;

        @Getter
        @Setter
        public static class Kakao {
            private String authorizationUri;
            private String tokenUri;
            private String userInfoUri;
            private String userNameAttribute;
        }
    }

}