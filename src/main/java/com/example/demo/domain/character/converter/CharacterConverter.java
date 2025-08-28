package com.example.demo.domain.character.converter;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import org.springframework.stereotype.Component;

@Component
public class CharacterConverter {

    /**
     * StoryCharacter -> CompletedCharacterResponse
     */
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

}