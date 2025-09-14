package com.example.demo.domain.story.converter;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;

public class StoryConverter {

    // StoryPage → StoryPageResponseDto 변환
    public static StoryPageResponseDto toStoryPageResponseDto(StoryPage storyPage, boolean includeCharacter) {
        Story story = storyPage.getStory();
        return StoryPageResponseDto.builder()
                .pageNumber(storyPage.getPageNumber())
                .content(storyPage.getContent())
                .imageUrl(storyPage.getImageUrl())
                .characterId(includeCharacter ? story.getCharacter().getId() : null)
                .build();
    }

}