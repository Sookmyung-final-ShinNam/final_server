package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.web.dto.StoryAdminResponseDto;
import java.util.List;

public interface StoryAdminQueryService {

    // 미완성 동화 조회
    List<StoryAdminResponseDto> getIncompleteStories();

}