package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.web.dto.StoryPageResponseDto;
import com.example.demo.domain.story.web.dto.StoryResponseDto;
import com.example.demo.domain.user.entity.User;

public interface StoryQueryService {

    /**
     * 동화 전체 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param user 현재 로그인한 사용자
     * @return StoryResponseDto - 페이징된 동화 목록과 메타데이터
    */
    StoryResponseDto getPagedStories(int page, int size, User user);

    /**
     * 특정 동화의 특정 페이지 조회
     * @param storyId 동화 ID
     * @param pageNumber 페이지 번호
     * @return StoryPageResponseDto - 해당 페이지의 내용
     */
    StoryPageResponseDto getStoryPage(Long storyId, int pageNumber);
}