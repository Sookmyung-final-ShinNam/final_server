package com.example.demo.domain.character.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.entity.UserCharacterFavorite;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterCommandServiceImpl implements CharacterCommandService {

    private final StoryCharacterRepository storyCharacterRepository;
    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;

    @Override
    @Transactional
    public String addFavorite(User user, Long characterId) {

        // 캐릭터가 존재하는지 확인
        StoryCharacter character = storyCharacterRepository.findById(characterId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CHARACTER_NOT_FOUND));

        // 이미 관심 캐릭터로 등록되어 있는지 확인
        if (userCharacterFavoriteRepository.existsByUserAndCharacter(user, character)) {
            throw new CustomException(ErrorStatus.CHARACTER_ALREADY_FAVORITE);
        }

        // 현재 사용자가 등록한 관심 캐릭터 수 확인
        long favoriteCount = userCharacterFavoriteRepository.countByUser(user);
        if (favoriteCount >= 5) {
            throw new CustomException(ErrorStatus.CHARACTER_FAVORITE_LIMIT_EXCEEDED);
        }

        // 관심 캐릭터로 등록
        UserCharacterFavorite favorite = UserCharacterFavorite.builder()
                .user(user)
                .character(character)
                .build();
        userCharacterFavoriteRepository.save(favorite);

        return "관심 캐릭터로 등록되었습니다.";
    }

    @Override
    @Transactional
    public String removeFavorite(User user, Long characterId) {

        // 캐릭터가 존재하는지 확인
        StoryCharacter character = storyCharacterRepository.findById(characterId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CHARACTER_NOT_FOUND));

        // 관심 캐릭터 여부 확인
        UserCharacterFavorite favorite = userCharacterFavoriteRepository.findByUserAndCharacter(user, character)
                .orElseThrow(() -> new CustomException(ErrorStatus.CHARACTER_NOT_FAVORITE));

        // 관심 캐릭터에서 제거
        userCharacterFavoriteRepository.delete(favorite);

        return "관심 캐릭터에서 제거되었습니다.";
    }

}