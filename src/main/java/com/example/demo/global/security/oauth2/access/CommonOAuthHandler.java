package com.example.demo.global.security.oauth2.access;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.config.ApplicationProperties;
import com.example.demo.domain.user.service.command.UserCommandService;
import com.example.demo.domain.user.web.dto.LoginResponseDto;
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

@Component
@RequiredArgsConstructor
public class CommonOAuthHandler extends OncePerRequestFilter {

    private final OAuthProperties oauthProperties;
    private final ApplicationProperties applicationUrlProperties;

    private final KakaoUserInfo kakaoUserInfo;
    private final UserCommandService userCommandService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 요청 경로가 카카오 로그인 콜백 URI 이면
        if (path.equals(oauthProperties.getKakaoPathUri())) {
            handleKakaoLogin(request, response); // 카카오 로그인 처리 로직 호출
            return; // 필터 체인 진행을 중단하고 여기서 응답 완료
        }

        // 카카오 로그인 URI 가 아닌 경우, 다음 필터로 요청을 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 카카오 로그인 요청을 처리하는 메서드
     * 인가 코드를 받아 액세스 토큰 획득, 사용자 정보 처리
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @throws IOException 입출력 예외
     */
    private void handleKakaoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String code = request.getParameter("code");
        if (code == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Authorization code is missing.");
            return;
        }

        try {

            // 1. 인가 코드를 사용하여 카카오 액세스 토큰 획득
            String accessToken = getKakaoAccessToken(code);

            // 2. 획득한 액세스 토큰으로 카카오 사용자 정보 조회
            UserInfo userInfo = kakaoUserInfo.getKakaoUserInfo(accessToken);

            // 3. 회원 정보가 없으면 신규 회원 등록, 있으면 기존 회원 정보 업데이트
            LoginResponseDto.Oauth2Result result = userCommandService.registerOrUpdateUser(userInfo.getEmail(), userInfo.getName());

            // 4. 리다이렉트
            // 임시 코드와 사용자 존재 여부(신규/기존)를 쿼리 파라미터로 포함
            String redirectUrl = String.format("%s?tempCode=%s&status=%b",
                    applicationUrlProperties.getRedirectUrl(), // 기본 URL
                    URLEncoder.encode(result.getTempCode(), "UTF-8"), // URL 인코딩 된 임시 코드
                    result.isNewUser()); // 사용자 존재 여부 (true : 기존, false : 신규)

            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", redirectUrl);

        } catch (Exception e) {
            // 토큰 획득 또는 처리 중 예외 발생 시, 500 Internal Server Error 응답
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to retrieve access token: " + e.getMessage());
        }
    }

    /**
     * 카카오 인가 코드를 사용하여 카카오 액세스 토큰 요청
     * @param code 카카오 인가 코드
     * @return 획득한 카카오 액세스 토큰
     * @throws CustomException 토큰 획득 실패 시 발생
     */
    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add("code", code);
        tokenRequest.add("client_id", oauthProperties.getRegistration().getKakao().getClientId());
        tokenRequest.add("client_secret", oauthProperties.getRegistration().getKakao().getClientSecret());
        tokenRequest.add("redirect_uri", applicationUrlProperties.getBaseUrl() + oauthProperties.getKakaoPathUri());
        tokenRequest.add("grant_type", oauthProperties.getRegistration().getKakao().getAuthorizationGrantType());

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HttpEntity 생성
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        // 카카오 토큰 발급 API에 POST 요청
        ResponseEntity<String> tokenResponse = restTemplate.exchange(
                oauthProperties.getProvider().getKakao().getTokenUri(),
                HttpMethod.POST,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 ObjectMapper
        try {
            // 응답 바디(JSON 문자열)를 JsonNode로 파싱
            JsonNode jsonNode = objectMapper.readTree(tokenResponse.getBody());
            return jsonNode.get("access_token").asText();
        } catch (IOException e) {
            // JSON 파싱 중 예외 발생 시, 예외
            throw new CustomException(ErrorStatus.TOKEN_INVALID_ACCESS_TOKEN);
        }
    }

}