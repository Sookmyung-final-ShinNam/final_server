package com.example.demo.domain.user.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
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

import java.time.LocalDateTime;
import java.util.List;
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
    private final ConversationSessionRepository conversationSessionRepository;
    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;
    private final StoryRepository storyRepository;

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

        return new LoginResponseDto.Oauth2Result(isNewUser.get(), user.isAgreedToTerms(), tempCode);
    }

    // 신규 유저 생성
    private User createNewUser(String email, String nickname) {
        User user = new User();
        user.setAgreedToTerms(false);
        user.setEmail(email);
        user.setNickname(nickname);
        user.setProfileImageUrl("https://avatars.githubusercontent.com/u/201584629?s=400&u=a82befb373a5969512867ca357157ba4a33bd279&v=4");
        user.setGrade(User.UserGrade.BASIC);
        user.setStatus(User.UserStatus.DELETED);
        user.setDeletedAt(BaseEntity.now());
        user.addPoints(5);
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

    @Override
    public String deactivateUser(User user) {
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
        return "로그아웃 성공";
    }

    @Override
    public String withdrawUser(User user) {
        user.setStatus(User.UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        return "회원 탈퇴 요청이 완료되었습니다. 오늘 자정에 계정이 삭제됩니다.";
    }

    @Override
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 관련 엔티티 삭제 (대화, 토큰, 즐겨찾기)
        tokenRepository.deleteAllByUser(user);
        conversationSessionRepository.deleteAllByUser(user);
        userCharacterFavoriteRepository.deleteAllByUser(user);

        // 스토리의 user를 null로 세팅
        List<Story> stories = storyRepository.findByUser(user);
        for (Story story : stories) {
            story.setUser(null);
        }
        storyRepository.saveAll(stories);

        // 사용자 삭제
        userRepository.delete(user);

    }

}