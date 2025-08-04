package com.example.demo.global.security.jwt;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@WebFilter("/*") // 모든 요청에 대해 필터 적용
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 인증이 필요 없는 경로 -> 필터 건너뛰기
        if (isPermittedRequest(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        try {
            // Authorization 헤더가 없거나 "Bearer "로 시작하지 않는 경우 예외 발생
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new CustomException(ErrorStatus.TOKEN_MISSING);
            }

            // "Bearer " 접두사를 제거하여 실제 토큰 문자열 추출
            String token = authorizationHeader.substring(7);

            // 1. JWT 유효성 검증 (서명, 만료 여부)
            jwtUtil.validateToken(token);

            // 2. 토큰이 데이터베이스에 등록되어 있는지 확인
            if (!jwtUtil.isTokenRegistered(token)) {
                throw new CustomException(ErrorStatus.TOKEN_NOT_FOUND);
            }

            // 3. 토큰에서 이메일 추출
            String email = jwtUtil.extractEmailFromToken(token);

            // 4. 사용자 상태 검증 (활성, 삭제 여부)
            jwtUtil.validateUserStatus(email);

            // 5. 이메일을 통해 사용자 객체 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

            // 6. 관리자 경로에 대한 관리자 역할 확인
            if (requestURI.startsWith("/api/admin")) {
                if (user.getGrade() != User.UserGrade.ADMIN) {
                    throw new CustomException(ErrorStatus.ADMIN_UNAUTHORIZED_ACCESS);
                }
            }

            // 7. SecurityContextHolder에 Authentication 객체 생성 및 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null); // 사용자 객체 설정 (권한은 필터에서 직접 확인하는 형식)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            // CustomException 발생 시 정의된 오류 응답 처리
            handleAuthenticationError(response, e);
        } catch (Exception e) {
            // 그 외 예상치 못한 예외 발생 시 내부 서버 오류 처리
            handleAuthenticationError(response, new CustomException(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 주어진 요청 URI가 인증이 필요 없는 허용된 경로인지 확인
     *
     * @param requestURI 들어오는 요청의 URI.
     * @return URI가 허용되면 true, 그렇지 않으면 false.
     */
    private boolean isPermittedRequest(String requestURI) {
        return requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/webjars") ||
                requestURI.equals("/favicon.ico") ||
                requestURI.startsWith("/api/permit/") || // 인증 없이 접근 가능한 API 경로
                requestURI.equals("/login"); // 로그인 경로는 인증 필터를 거치지 않음
    }

    /**
     * 인증 오류를 처리하고 구조화된 API 응답 전송
     *
     * @param response 오류를 작성할 HttpServletResponse
     * @param e        발생한 예외
     * @throws IOException 응답을 작성하는 동안 I/O 오류가 발생하면
     */
    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        ErrorStatus errorStatus;
        String errorMessage;

        if (e instanceof CustomException customException) {
            errorStatus = customException.getErrorStatus();
            errorMessage = customException.getMessage();
        } else {
            // 예상치 못한 오류에 대한 기본값 설정
            errorStatus = ErrorStatus.COMMON_UNAUTHORIZED;
            errorMessage = "Authentication failed: " + e.getMessage();
        }

        // ApiResponse 객체 생성
        ApiResponse<Object> apiResponse = ApiResponse.onFailure(
                errorStatus, // BaseErrorCode 타입의 errorStatus 객체 전달
                errorMessage // 상세 메시지 전달
        );

        // HTTP 응답 상태 코드 설정
        response.setStatus(errorStatus.getHttpStatus().value());

        // 응답 콘텐츠 타입 및 문자 인코딩 설정
        response.setContentType("application/json; charset=UTF-8");

        // ObjectMapper를 사용하여 ApiResponse 객체를 JSON 문자열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        // 응답 본문에 JSON 문자열 작성
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

}