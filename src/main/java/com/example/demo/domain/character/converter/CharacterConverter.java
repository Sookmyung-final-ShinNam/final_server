package com.example.demo.domain.character.converter;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import org.springframework.stereotype.Component;

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

}