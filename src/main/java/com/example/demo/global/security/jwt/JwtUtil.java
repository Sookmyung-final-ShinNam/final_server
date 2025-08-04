package com.example.demo.global.security.jwt;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.config.ApplicationProperties;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.TokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final ApplicationProperties applicationUrlProperties;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    // JWT 서명에 사용될 키를 디코딩하여 반환
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 현재 시간을 애플리케이션의 타임존에 맞춰 반환
    private LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(applicationUrlProperties.getTimezone()));
    }

    /**
     * Access Token 생성
     *
     * @param email 토큰에 포함될 사용자 이메일
     * @return 생성된 Access Token 문자열
     */
    public String generateAccessToken(String email) {
        LocalDateTime now = now();
        return generateToken(email, now, jwtProperties.getTokenValidity().getAccessToken());
    }

    /**
     * Refresh Token을 생성
     *
     * @param email 토큰에 포함될 사용자 이메일
     * @return 생성된 Refresh Token 문자열
     */
    public String generateRefreshToken(String email) {
        LocalDateTime now = now();
        return generateToken(email, now, jwtProperties.getTokenValidity().getRefreshToken());
    }

    // 주어진 이메일, 현재 시간, 유효 기간을 사용하여 JWT 생성
    private String generateToken(String email, LocalDateTime now, long validitySeconds) {
        Date issuedAt = Date.from(now.atZone(ZoneId.of(applicationUrlProperties.getTimezone())).toInstant());
        Date expiration = Date.from(now.plusSeconds(validitySeconds).atZone(ZoneId.of(applicationUrlProperties.getTimezone())).toInstant());

        return Jwts.builder()
                .claim("email", email) // 이메일 클레임 추가
                .setIssuedAt(issuedAt) // 발행 시간 설정
                .setExpiration(expiration) // 만료 시간 설정
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 서명 키와 알고리즘 설정
                .compact(); // JWT 생성
    }

    /**
     * JWT에서 이메일 클레임 추출
     * 토큰이 유효하지 않으면 CustomException 발생
     *
     * @param token 이메일을 추출할 JWT
     * @return 추출된 이메일 문자열
     * @throws CustomException 토큰이 유효하지 않을 경우
     */
    public String extractEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey()) // 서명 키 설정
                    .parseClaimsJws(token) // 토큰 파싱
                    .getBody(); // 클레임 바디 추출

            return claims.get("email", String.class); // 이메일 클레임 반환
        } catch (JwtException e) {
            throw new CustomException(ErrorStatus.TOKEN_INVALID_ACCESS_TOKEN);
        }
    }

    /**
     * JWT의 유효성 검증 (서명, 만료)
     *
     * @param token 검증할 JWT
     * @throws CustomException 토큰이 유효하지 않거나 만료되었을 경우
     */
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // secret key는 Key 타입이어야 함
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 검증
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomException(ErrorStatus.TOKEN_INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorStatus.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorStatus.TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorStatus.TOKEN_CLAIMS_EMPTY);
        }
    }

    /**
     * 주어진 토큰이 데이터베이스에 등록되어 있는지 확인
     * Access Token 또는 Refresh Token으로 등록되어 있으면 true 반환
     *
     * @param token 확인할 토큰 문자열
     * @return 토큰이 데이터베이스에 등록되어 있으면 true, 그렇지 않으면 false
     */
    public boolean isTokenRegistered(String token) {
        return tokenRepository.findByAccessToken(token).isPresent() || tokenRepository.findByRefreshToken(token).isPresent();
    }

    /**
     * 사용자 상태 검증
     * 사용자가 존재하지 않거나, 삭제되었거나, 비활성 상태이면 CustomException 발생
     *
     * @param email 검증할 사용자의 이메일
     * @throws CustomException 사용자가 존재하지 않거나, 삭제되었거나, 비활성 상태일 경우
     */
    public void validateUserStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        if (User.UserStatus.DELETED.equals(user.getStatus())) {
            throw new CustomException(ErrorStatus.USER_ALREADY_DELETED);
        }

        if (!User.UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new CustomException(ErrorStatus.USER_ALREADY_LOGOUT);
        }
    }

}