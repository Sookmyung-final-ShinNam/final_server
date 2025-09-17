package com.example.demo.domain.character.converter;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
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

    // List<StoryCharacter> → CompletedCharacterResponse.CharacterListResponse
    public CompletedCharacterResponse.CharacterListResponse toCharacterListResponse(
            List<StoryCharacter> characters,
            Set<Long> favoriteIds
    ) {
        List<CompletedCharacterResponse> dtoList = characters.stream()
                .map(ch -> toCompletedCharacterResponse(ch, favoriteIds.contains(ch.getId())))
                .toList();

        return CompletedCharacterResponse.CharacterListResponse.builder()
                .characters(dtoList)
                .build();
    }

}