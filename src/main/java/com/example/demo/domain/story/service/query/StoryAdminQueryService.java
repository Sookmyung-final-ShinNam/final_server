package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.web.dto.StoryAdminResponseDto;
import com.example.demo.domain.story.web.dto.StoryFailedAdminResponseDto;

import java.util.List;

public interface StoryAdminQueryService {

    // 미완성 동화 조회
    List<StoryAdminResponseDto> getIncompleteStories();

    // 스토리 재생성 배치 실패 동화 조회
    List<StoryFailedAdminResponseDto> getFailedRetryStories();
}