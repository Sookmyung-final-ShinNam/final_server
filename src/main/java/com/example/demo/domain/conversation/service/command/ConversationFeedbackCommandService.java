package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;

public interface ConversationFeedbackCommandService {

    /**
     * 사용자 답변에 대한 LLM 피드백 처리
     *
     * @param messageId StepAttempt 또는 Step 기준 ID
     * @param userAnswer 사용자 입력 답변
     * @return 피드백 결과 (정답 여부 + 메시지)
     */
    ConversationResponseDto.FeedbackResponseDto handleFeedback(Long messageId, String userAnswer);
}