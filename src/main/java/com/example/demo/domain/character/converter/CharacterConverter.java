package com.example.demo.domain.character.converter;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CharacterConverter {

    // 목록용 변환
    public CompletedCharacterResponse toCompletedCharacterResponse(StoryCharacter ch, boolean important) {
        return CompletedCharacterResponse.builder()
                .characterId(ch.getId())
                .name(ch.getName())
                .gender(ch.getGender().name())
                .imageUrl(ch.getImageUrl())
                .important(important)
                .createTime(ch.getCreatedAt())
                .build();
    }

    // 상세용 변환
    public CompletedCharacterResponse.Detail toDetailCharacterResponse(StoryCharacter ch, boolean important) {
        return CompletedCharacterResponse.Detail.builder()
                .characterId(ch.getId())
                .name(ch.getName())
                .gender(ch.getGender().name())
                .age(ch.getAge())
                .imageUrl(ch.getImageUrl())
                .personality(ch.getPersonality())
                .important(important)
                .createTime(ch.getCreatedAt())
                .storyId(ch.getStory().getId())
                .storyTitle(ch.getStory().getTitle())
                .build();
    }

    // Page<StoryCharacter> → CompletedCharacterResponse.CharacterListResponse
    public CompletedCharacterResponse.CharacterListResponse toCharacterListResponse(
            Page<StoryCharacter> characters,
            Set<Long> favoriteIds
    ) {
        List<CompletedCharacterResponse> dtoList = characters.stream()
                .map(ch -> toCompletedCharacterResponse(ch, favoriteIds.contains(ch.getId())))
                .sorted((a, b) -> {
                    if (a.isImportant() == b.isImportant()) {
                        return b.getCreateTime().compareTo(a.getCreateTime());
                    }
                    return a.isImportant() ? -1 : 1;
                })
                .toList();

        return CompletedCharacterResponse.CharacterListResponse.builder()
                .characters(dtoList)
                .currentPage(characters.getNumber())
                .totalPages(characters.getTotalPages())
                .totalElements(characters.getTotalElements())
                .hasNext(characters.hasNext())
                .build();
    }

}