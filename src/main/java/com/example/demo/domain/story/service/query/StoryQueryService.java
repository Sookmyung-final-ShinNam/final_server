package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.web.dto.StoryPageResponseDto;

public interface StoryQueryService {

    /**
     * 특정 동화의 특정 페이지 조회
     * @param storyId 동화 ID
     * @param pageNumber 페이지 번호
     * @return StoryPageResponseDto - 해당 페이지의 내용
     */
    StoryPageResponseDto getStoryPage(Long storyId, int pageNumber);
}