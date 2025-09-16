package com.example.demo.domain.story.converter;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;

import java.util.stream.Collectors;

public class StoryConverter {

    // StoryPage → StoryPageResponseDto 변환
    public static StoryPageResponseDto toStoryPageResponseDto(Story story, StoryPage storyPage, int pageNumber) {

        // 0페이지 → 동화 인트로
        if (pageNumber == 0) {
            // 마지막 페이지 이미지/영상
            StoryPage lastPage = story.getStoryPages().stream()
                    .filter(p -> p.getPageNumber() == 4)
                    .findFirst()
                    .orElse(null);

            StoryPageResponseDto.StoryContent storyContent = new StoryPageResponseDto.StoryContent(
                    null,
                    lastPage != null ? lastPage.getImageUrl() : null,
                    lastPage != null ? lastPage.getVideoUrl() : null
            );

            return StoryPageResponseDto.builder()
                    .storyId(story.getId())
                    .pageNumber(0)
                    .title(story.getTitle())
                    .storyThemes(story.getStoryThemes().stream()
                            .map(storyTheme -> storyTheme.getTheme().getName())
                            .collect(Collectors.toSet()))
                    .storyBackgrounds(story.getStoryBackgrounds().stream()
                            .map(storyBackground -> storyBackground.getBackground().getName())
                            .collect(Collectors.toSet()))
                    .description(story.getDescription())
                    .storyContent(storyContent)
                    .build();
        }

        // 1~4페이지 → 본문
        StoryPageResponseDto.StoryContent storyContent = new StoryPageResponseDto.StoryContent(
                storyPage.getContent(),
                storyPage.getImageUrl(),
                storyPage.getVideoUrl()
        );

        return StoryPageResponseDto.builder()
                .storyId(story.getId())
                .pageNumber(pageNumber)
                .storyContent(storyContent)
                .build();
    }

}