package com.example.demo.global.security.oauth2.config;

import com.example.demo.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.*;

@Configuration
@RequiredArgsConstructor
public class OAuthRegistrationConfig {

    private final OAuthProperties oAuthProperties;
    private final ApplicationProperties applicationUrlProperties;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        OAuthProperties.Registration.Kakao kakao = oAuthProperties.getRegistration().getKakao();
        OAuthProperties.Provider.Kakao kakaoProvider = oAuthProperties.getProvider().getKakao();

        ClientRegistration kakaoRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId(kakao.getClientId())
                .clientSecret(kakao.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(applicationUrlProperties.getBaseUrl() + kakao.getRedirectUri())
                .scope(kakao.getScope())
                .authorizationUri(kakaoProvider.getAuthorizationUri())
                .tokenUri(kakaoProvider.getTokenUri())
                .userInfoUri(kakaoProvider.getUserInfoUri())
                .userNameAttributeName(kakaoProvider.getUserNameAttribute())
                .clientName(kakao.getClientName())
                .build();

        return new InMemoryClientRegistrationRepository(kakaoRegistration);
    }

}