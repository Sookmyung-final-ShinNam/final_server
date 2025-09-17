package com.example.demo.domain.character.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.converter.CharacterConverter;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterQueryServiceImpl implements CharacterQueryService {

    private final StoryCharacterRepository storyCharacterRepository;
    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;

    private final CharacterConverter characterConverter;
    private final UserRepository userRepository;

    @Override
    public CompletedCharacterResponse.CharacterListResponse getCompletedCharacters(User user, StoryCharacter.Gender gender) {

        // 1. 유저의 모든 캐릭터 가져오기
        List<StoryCharacter> characters = storyCharacterRepository.findByUser(user);

        // 2. 성별 필터링 (null 이면 전체)
        if (gender != null) {
            characters = characters.stream()
                    .filter(c -> c.getGender() == gender)
                    .toList();
        }

        // 3. 관심 캐릭터 ID set
        Set<Long> favoriteIds = userCharacterFavoriteRepository.findByUser(user).stream()
                .map(fav -> fav.getCharacter().getId())
                .collect(Collectors.toSet());

        // 4. 정렬 (미완성 → 관심 → 최신순)
        List<StoryCharacter> sorted = characters.stream()
                .sorted(Comparator
                        // 미완성(true) → 완성(false) → true < false 이므로 "미완성 먼저"
                        .comparing((StoryCharacter c) -> c.getStatus() != StoryCharacter.CharacterStatus.COMPLETED)
                        // 관심(true) → 비관심(false) → false < true 이므로 "관심 먼저"
                        .thenComparing(c -> !favoriteIds.contains(c.getId()))
                        // 최신순
                        .thenComparing(StoryCharacter::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        // 5. DTO 변환
        return characterConverter.toCharacterListResponse(sorted, favoriteIds);
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