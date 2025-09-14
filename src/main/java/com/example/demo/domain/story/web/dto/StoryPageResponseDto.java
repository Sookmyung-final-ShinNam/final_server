package com.example.demo.domain.story.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryPageResponseDto {
    private int pageNumber;
    private String content;
    private String imageUrl;
    private String videoUrl;     // 영상이 있을 경우에만 값 존재
    private Long characterId;   // 첫 장/마지막 장일 때만 값 존재
}