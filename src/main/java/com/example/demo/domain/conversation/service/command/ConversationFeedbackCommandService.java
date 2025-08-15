package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;

public interface ConversationFeedbackCommandService {

    /**
     * 피드백 요청을 처리하고, LLM에 피드백을 전달하여 응답을 반환
     *
     * @param messageId 메시지 Id
     * @param userAnswer 사용자 응답
     * @return 피드백 응답 DTO
     */
    ConversationResponseDto.FeedbackResponseDto handleFeedback(Long messageId, String userAnswer);
}