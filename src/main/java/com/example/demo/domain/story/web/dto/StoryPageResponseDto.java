package com.example.demo.domain.story.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryPageResponseDto {
    private int pageNumber;
    private String content;
    private String imageUrl;
}