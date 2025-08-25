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

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController extends AuthController {

    private final StoryQueryService storyQueryService;

    @Operation(
            summary = "동화 전체 조회",
            description = """
                    - 12개씩 페이징
                    - 페이징 기준 : 미완성 / important / createTime
                    - 완성/미완성 여부를 반환
                    - 미완성이면 이어하기 가능한 형태로!!
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping
    public ApiResponse<List<StoryResponseDto>> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, storyQueryService.getPagedStories(page, size, user)
        );
    }

}