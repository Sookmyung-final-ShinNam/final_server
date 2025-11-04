package com.example.demo.domain.story.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.service.command.StoryAdminCommandService;
import com.example.demo.domain.story.service.query.StoryAdminQueryService;
import com.example.demo.domain.story.web.dto.StoryAdminResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stories")
@RequiredArgsConstructor
public class StoryAdminController {

    private final StoryAdminQueryService storyAdminQueryService;
    private final StoryAdminCommandService storyAdminCommandService;

    @Operation(summary = "링크 누락된 동화 조회 (이미지나 영상 링크 없는 완성된 동화)")
    @GetMapping("/incomplete")
    public ApiResponse<List<StoryAdminResponseDto>> getIncompleteStories() {
        return ApiResponse.of(SuccessStatus._OK, storyAdminQueryService.getIncompleteStories());
    }


}