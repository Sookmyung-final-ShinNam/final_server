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
            summary = "동화 생성 완성 (마지막 Feedback 이후 호출)",
            description = """
                    동기 - 바로 응답을 줍니다.
                    비동기 - 내용 기반으로 아래 정보들을 업데이트/생성합니다.
                            - 동화 정보 업데이트(제목, 3줄 요약)
                            - 캐릭터 정보 업데이트(성격, 기본 이미지)
                            - 동화 페이지 생성(정리된 내용, 각 페이지별 이미지 or 비디오)
                    imageType 에는 <image, video> 중 하나만 입력 가능합니다!
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