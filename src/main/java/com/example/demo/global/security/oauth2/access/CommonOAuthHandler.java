package com.example.demo.global.security.oauth2.access;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.config.ApplicationProperties;
import com.example.demo.domain.user.repository.TokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.security.oauth2.config.OAuthProperties;
import com.example.demo.global.security.oauth2.info.KakaoUserInfo;
import com.example.demo.global.security.oauth2.info.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommonOAuthHandler extends OncePerRequestFilter {

    private final OAuthProperties oauthProperties;
    private final ApplicationProperties applicationUrlProperties;

    private final KakaoUserInfo kakaoUserInfo;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.equals(oauthProperties.getKakaoPathUri())) {
            handleKakaoLogin(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleKakaoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String code = request.getParameter("code");
        if (code == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Authorization code is missing.");
            return;
        }

        try {

            String accessToken = getKakaoAccessToken(code);
            UserInfo user = kakaoUserInfo.getKakaoUserInfo(accessToken);

            boolean isUserExists = userRepository.findByEmail(user.getEmail()).isPresent();

            // 임시 코드 생성
            String tempCode = null;
            int retry = 3;
            for (int i = 0; i < retry; i++) {
                String candidate = UUID.randomUUID().toString();

                boolean exists = tokenRepository.existsByTempCode(candidate);
                if (!exists) {
                    tempCode = candidate;
                    break;
                }
            }

            if (tempCode == null) {
                throw new CustomException(ErrorStatus.TOKEN_GENERATION_FAILED);
            }

            if (isUserExists) {
                // 기존 회원
            } else {
                // 신규 회원 (일단 가입은 하지 않음 -> delete 로 설정해서 자동으로 소프트 딜리트 되도록)
            }

            // 임시 코드와 회원 여부를 포함하여 리다이렉트
            String redirectUrl = String.format("%s?tempCode=%s&status=%b",
                    applicationUrlProperties.getRedirectUrl(),
                    URLEncoder.encode(tempCode, "UTF-8"),
                    isUserExists);
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", redirectUrl);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to retrieve access token: " + e.getMessage());
        }
    }

    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("code", code);
        tokenRequest.add("client_id", oauthProperties.getRegistration().getKakao().getClientId());
        tokenRequest.add("client_secret", oauthProperties.getRegistration().getKakao().getClientSecret());
        tokenRequest.add("redirect_uri", applicationUrlProperties.getBaseUrl() + oauthProperties.getKakaoPathUri());
        tokenRequest.add("grant_type", oauthProperties.getRegistration().getKakao().getAuthorizationGrantType());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenRequest, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> tokenResponse = restTemplate.exchange(
                oauthProperties.getProvider().getKakao().getTokenUri(),
                HttpMethod.POST,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(tokenResponse.getBody());
            return jsonNode.get("access_token").asText();
        } catch (IOException e) {
            throw new CustomException(ErrorStatus.TOKEN_INVALID_ACCESS_TOKEN);
        }
    }

}