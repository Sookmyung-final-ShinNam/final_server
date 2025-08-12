package com.example.demo.domain.conversation.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
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
            @Valid @RequestBody ConversationRequestDto.FeedbackRequestDto request
    ) {
        return ApiResponse.of(SuccessStatus._OK, conversationFeedbackCommandService.handleFeedback(request));
    }

}