package com.example.demo.domain.user.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.Token;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.TokenRepository;
import com.example.demo.domain.user.web.dto.LoginResponseDto;
import com.example.demo.global.security.jwt.JwtProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserQueryServiceImpl implements UserQueryService {

    private final TokenRepository tokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * 주어진 tempCode가 존재하는지 확인하고, 해당 토큰 정보 반환
     *
     * @param tempCode 임시 코드
     * @return 토큰 정보가 담긴 LoginResult
     */
    @Override
    public LoginResponseDto.LoginResult findTokenByTempCode(String tempCode) {

        // 토큰 조회
        Token token = tokenRepository.findByTempCode(tempCode)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 토큰 업데이트 시점 + 유효 기간을 계산하여 만료 시간 설정
        LocalDateTime createdAt = token.getUpdatedAt();
        long accessTokenValiditySeconds = jwtProperties.getTokenValidity().getAccessToken();
        LocalDateTime accessTokenExpiredAt = createdAt.plusSeconds(accessTokenValiditySeconds);

        // 유저 조회
        User user = token.getUser();

        return new LoginResponseDto.LoginResult(
                user.getEmail(),
                token.getAccessToken(),
                token.getRefreshToken(),
                accessTokenExpiredAt
        );
    }

}