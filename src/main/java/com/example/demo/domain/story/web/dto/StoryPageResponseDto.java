package com.example.demo.domain.story.web.dto;

import com.example.demo.domain.story.entity.StoryBackground;
import com.example.demo.domain.story.entity.StoryTheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class StoryPageResponseDto {

    private Long storyId;                  // 모든 페이지 공통
    private Integer pageNumber;            // 모든 페이지 공통

    private String title;                  // 0 페이지
    private Set<StoryTheme> storyThemes;   // 0 페이지
    private Set<StoryBackground> storyBackgrounds; // 0 페이지
    private String description;            // 0 페이지

    private StoryContent storyContent;     // 모든 페이지 공통

    @Getter
    @AllArgsConstructor
    public static class StoryContent {
        private String content;   // 본문 내용 (1~4 페이지만 있음)
        private String imageUrl;  // 이미지 (0~4 페이지 모두 있음. 0페이지는 4페이지와 같음)
        private String videoUrl;  // 영상 (0~4 페이지 모두 있음. 0페이지는 4페이지와 같음)
    }

}