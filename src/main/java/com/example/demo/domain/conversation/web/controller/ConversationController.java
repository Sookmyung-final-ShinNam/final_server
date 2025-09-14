package com.example.demo.domain.conversation.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.conversation.service.async.ConversationAsyncService;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.conversation.service.command.ConversationFeedbackCommandService;
import com.example.demo.domain.conversation.service.command.ConversationStartCommandService;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 대화 컨트롤러
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController extends AuthController {

    private final ConversationQueryService conversationQueryService;
    private final ConversationStartCommandService conversationStartCommandService;
    private final ConversationFeedbackCommandService conversationFeedbackCommandService;
    private final ConversationAsyncService conversationAsyncService;
    private final ConversationCompleteCommandService conversationCompleteCommandService;

    @Operation(
            summary = "대화 세션 시작",
            description = """
                    사용자 정보를 검증한 뒤 gui 정보들을 기반으로 Story / Character / Session을 생성하고
                    LLM을 호출하여 첫 문장을 생성합니다.
                    
                    동기 처리 : Story 생성, Character 생성, 첫 메시지 저장, 응답 반환
                    비동기 처리 : 다음 스텝(next-step=STEP_01) 사전 생성
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/start")
    public ApiResponse<ConversationResponseDto.ConversationStartResponseDto> startConversation(
            @Parameter(description = "gui 로 수집한 정보들", required = true)
            @Valid @RequestBody ConversationRequestDto.ConversationStartRequestDto request
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, conversationStartCommandService.startConversation(request, user));
    }

    @Operation(
            summary = "다음 스텝 메시지 조회",
            description = """
                    다음 세션의 next-story와 llmQuestion이 있으면 응답, 없으면 상태 PENDING 반환
                    조회하고 싶은 단계를 입력하세요. ex. currentStep 에 STEP_01 입력시 STEP_01 의 nextStory 와 llmQuestion 반환 
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/next-step")
    public ApiResponse<Object> getNextStep(
            @RequestParam Long sessionId,
            @RequestParam String currentStep
    ) {
        return ApiResponse.of(SuccessStatus._OK, conversationQueryService.getNextStepMessage(sessionId, currentStep));
    }

    @Operation(
            summary = "사용자 답변 피드백",
            description = """
                    사용자 답변에 대한 긍정/부정 평가와 피드백 제공
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/feedback")
    public ApiResponse<ConversationResponseDto.FeedbackResponseDto> feedback(
            @RequestParam Long messageId,
            @RequestParam String userAnswer
    ) {
        return ApiResponse.of(SuccessStatus._OK, conversationFeedbackCommandService.handleFeedback(messageId, userAnswer));
    }

    @Operation(
            summary = "동화 생성 완성 (마지막 Feedback 이후 호출)",
            description = """
                    동기 - 바로 응답을 줍니다.
                    비동기 - 내용 기반으로 아래 정보들을 업데이트/생성합니다.
                            - 동화 정보 업데이트(제목, 3줄 요약)
                            - 캐릭터 정보 업데이트(성격, 기본 이미지)
                            - 동화 페이지 생성(정리된 내용, 각 페이지별 이미지)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/complete")
    public ApiResponse<Void> storyComplete(
            @RequestParam Long sessionId
    ) {
        conversationAsyncService.storyComplete(sessionId);
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(
            summary = "페이지별 동영상 생성",
            description = """
                    각 페이지에 맞는 동영상을 생성합니다. 
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/video")
    public ApiResponse<Void> storyToVideo(
            @RequestParam Long storyId
    ) {
        conversationCompleteCommandService.generateStoryMedia(storyId, "video");
        return ApiResponse.of(SuccessStatus._OK);
    }

}