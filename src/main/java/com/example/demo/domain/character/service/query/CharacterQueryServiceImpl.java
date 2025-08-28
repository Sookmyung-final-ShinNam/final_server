package com.example.demo.domain.character.service.query;

import com.example.demo.domain.character.converter.CharacterConverter;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;
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

    @Override
    public Page<CompletedCharacterResponse> getCompletedCharacters(User user, int page, int size) {

        // 페이징 (DB 기준은 createdAt desc)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<StoryCharacter> characters = storyCharacterRepository.findByStatus(
                StoryCharacter.CharacterStatus.COMPLETED, pageRequest);

        // 유저의 관심 캐릭터 ID set
        Set<Long> favoriteIds = user.getFavorites().stream()
                .map(fav -> fav.getCharacter().getId())
                .collect(Collectors.toSet());

        // Entity → DTO 변환
        Page<CompletedCharacterResponse> dtoPage = characters.map(ch ->
                characterConverter.toCompletedCharacterResponse(ch, favoriteIds.contains(ch.getId()))
        );

        // 중요도(important) true 먼저 정렬 → 최신순
        return new PageImpl<>(
                dtoPage.getContent().stream()
                        .sorted((a, b) -> {
                            if (a.isImportant() == b.isImportant()) {
                                return b.getCreateTime().compareTo(a.getCreateTime());
                            }
                            return a.isImportant() ? -1 : 1;
                        })
                        .toList(),
                pageRequest,
                dtoPage.getTotalElements()
        );
    }

}