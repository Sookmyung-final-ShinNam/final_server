package com.example.demo.domain.story.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.story.service.query.StoryQueryService;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController extends AuthController {

    private final StoryQueryService storyQueryService;

    @Operation(
            summary = "동화 페이지별 내용 조회",
            description = """
            - storyId와 pageNumber로 특정 동화 페이지 조회 (0~4)
            - 단, '완성된 동화'만 상세 조회 가능
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/{storyId}/page/{pageNumber}")
    public ApiResponse<StoryPageResponseDto> getStoryPage(
            @PathVariable Long storyId,
            @PathVariable int pageNumber
    ) {
        return ApiResponse.of(SuccessStatus._OK, storyQueryService.getStoryPage(storyId, pageNumber));
    }

}