package com.example.demo.domain.story.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.story.service.query.StoryQueryService;
import com.example.demo.domain.story.web.dto.StoryResponseDto;
import com.example.demo.domain.user.entity.User;
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
            summary = "동화 전체 조회",
            description = """
                    - 12개씩 페이징
                    - 페이징 기준 : 미완성 / 관심동화 / createTime 빠른 순서 
                    - 완성/미완성 여부를 반환
                    - 미완성이면 이어하기 가능한 형태로!! (canContinue, sessionId + currentStep)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping
    public ApiResponse<StoryResponseDto> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size)
    {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, storyQueryService.getPagedStories(page, size, user)
        );
    }

}