package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.web.dto.StoryResponseDto;
import com.example.demo.domain.user.entity.User;

import java.util.List;

public interface StoryQueryService {

    List<StoryResponseDto> getPagedStories(int page, int size, User user);

}