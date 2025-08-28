package com.example.demo.domain.character.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.converter.CharacterConverter;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterQueryServiceImpl implements CharacterQueryService {

    private final StoryCharacterRepository storyCharacterRepository;
    private final CharacterConverter characterConverter;
    private final UserRepository userRepository;

    @Override
    public CompletedCharacterResponse.CharacterListResponse getCompletedCharacters(User user, int page, int size) {

        // 1. User와 favorites 한 번에 조회
        User fullUser = userRepository.findByIdWithFavorites(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 2. 페이징 (DB 기준 createdAt desc)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StoryCharacter> characters = storyCharacterRepository.findByStatus(
                StoryCharacter.CharacterStatus.COMPLETED, pageRequest);

        // 3. 관심 캐릭터 ID set
        Set<Long> favoriteIds = fullUser.getFavorites().stream()
                .map(fav -> fav.getCharacter().getId())
                .collect(Collectors.toSet());

        // 4. 컨버터를 통해 CharacterListResponse 반환
        return characterConverter.toCharacterListResponse(characters, favoriteIds);
    }

    @Override
    public CompletedCharacterResponse.Detail getCharacterDetail(User user, Long characterId) {

        // 1. User + favorites 한 번에 조회
        User fullUser = userRepository.findByIdWithFavorites(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 2. 캐릭터 조회 (완료된 캐릭터만)
        StoryCharacter character = storyCharacterRepository.findByIdAndStatus(
                characterId, StoryCharacter.CharacterStatus.COMPLETED
        ).orElseThrow(() -> new CustomException(ErrorStatus.CHARACTER_NOT_FOUND));

        // 3. 관심 캐릭터 여부 확인
        boolean isFavorite = fullUser.getFavorites().stream()
                .anyMatch(fav -> fav.getCharacter().getId().equals(characterId));

        // 4. Entity → 상세 DTO 변환
        return characterConverter.toDetailCharacterResponse(character, isFavorite);
    }

}