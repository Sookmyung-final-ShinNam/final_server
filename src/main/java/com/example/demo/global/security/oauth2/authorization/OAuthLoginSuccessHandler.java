package com.example.demo.global.security.oauth2.authorization;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        String code = request.getParameter("code");  // URL 파라미터로 받은 인가 코드
        String state = request.getParameter("state");  // URL 파라미터로 받은 state 값

        if (code == null || state == null) {
            throw new CustomException(ErrorStatus.OAUTH_PROCESSING_FAILED);
        }

    }
}