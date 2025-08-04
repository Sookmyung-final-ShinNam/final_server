package com.example.demo.global.security.oauth2.info;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.global.security.oauth2.config.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component
@RequiredArgsConstructor
public class KakaoUserInfo {

    private final OAuthProperties oAuthProperties;

    public UserInfo getKakaoUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    oAuthProperties.getProvider().getKakao().getUserInfoUri(),
                    HttpMethod.GET,
                    new org.springframework.http.HttpEntity<>(headers),
                    String.class);

            String responseBody = response.getBody();

            String nickname = responseBody.contains("nickname") ?
                    responseBody.split("nickname\":\"")[1].split("\"")[0] : "닉네임 없음";

            String email = null;
            if (responseBody.contains("email")) {
                email = responseBody.split("email\":\"")[1].split("\"")[0];
            }

            if (email == null || email.isEmpty()) {
                throw new CustomException(ErrorStatus.EMAIL_NOT_FOUND);  // 이메일 없으면 예외
            }

            return new UserInfo(nickname, email);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.EMAIL_NOT_FOUND);
        }
    }

}