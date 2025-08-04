package com.example.demo.domain.user.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.Token;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.TokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.query.UserQueryService;
import com.example.demo.domain.user.web.dto.LoginResponseDto;
import com.example.demo.global.entity.BaseEntity;
import com.example.demo.global.security.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final JwtUtil jwtUtil;

    private final UserQueryService userQueryService;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    /**
     * 사용자 정보를 등록 or 업데이트
     * 신규 사용자인 경우 등록하고, 기존 사용자인 경우 업데이트
     *
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @return 신규 회원 여부와 tempCode
     */
    @Override
    public LoginResponseDto.Oauth2Result registerOrUpdateUser(String email, String nickname) {

        // 유니크한 tmpCode 생성
        String tempCode = null;
        int retry = 3;

        for (int i = 0; i < retry; i++) {
            String candidate = UUID.randomUUID().toString();
            if (!tokenRepository.existsByTempCode(candidate)) {
                tempCode = candidate;
                break;
            }
        }

        if (tempCode == null) {
            throw new CustomException(ErrorStatus.TOKEN_GENERATION_FAILED);
        }

        // 기존 유저 반환 혹은 신규 유저 생성
        AtomicBoolean isNewUser = new AtomicBoolean(true);

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    tokenRepository.deleteAllByUser(existingUser); // 기존 토큰 삭제
                    isNewUser.set(false);
                    return existingUser;
                })
                .orElseGet(() -> createNewUser(email, nickname));

        // 토큰 업데이트
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);
        saveTokenForUser(user, tempCode, accessToken, refreshToken);

        return new LoginResponseDto.Oauth2Result(isNewUser.get(), tempCode);
    }

    // 신규 유저 생성
    private User createNewUser(String email, String nickname) {
        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setProfileImageUrl("https://avatars.githubusercontent.com/u/201584629?s=400&u=a82befb373a5969512867ca357157ba4a33bd279&v=4");
        user.setCurrentPoints(500);
        user.setGrade(User.UserGrade.BASIC);
        user.setStatus(User.UserStatus.DELETED);
        user.setDeletedAt(BaseEntity.now());
        return userRepository.save(user);
    }

    // 토큰 업데이트
    private void saveTokenForUser(User user, String tempCode, String accessToken, String refreshToken) {
        Token token = new Token();
        token.setUser(user);
        token.setTempCode(tempCode);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        tokenRepository.save(token);
    }

    /**
     * 임시 코드(tempCode)를 사용하여 사용자 로그인 처리
     * tempCode로 토큰 조회 후 사용자 활성화 및 토큰 반환
     *
     * @param tempCode 임시 코드
     * @return 로그인 결과가 담긴 LoginResult
     */
    @Override
    public LoginResponseDto.LoginResult loginUser(String tempCode) {

        // tempCode 로 토큰 조회 및 반환 정보 생성
        LoginResponseDto.LoginResult loginResult = userQueryService.findTokenByTempCode(tempCode);

        // accessToken 으로 사용자 조회
        Token token = tokenRepository.findByAccessToken(loginResult.getAccessToken())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        User user = token.getUser();

        // 사용자 활성화
        user.activate();
        userRepository.save(user);

        return loginResult;
    }

}