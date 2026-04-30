package com.example.demo.domain.story.web.dto;

import com.example.demo.domain.story.entity.Story;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoryFailedAdminResponseDto {

    private Long storyId;
    private int retryCount;
    private String storyStatus;
    private LocalDateTime updatedAt;

    public static StoryFailedAdminResponseDto from(Story story) {

        return StoryFailedAdminResponseDto.builder()
                .storyId(story.getId())
                .retryCount(story.getRetryCount())
                .storyStatus(story.getStoryStatus().name())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
}