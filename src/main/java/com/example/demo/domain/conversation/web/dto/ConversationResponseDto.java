package com.example.demo.domain.conversation.web.dto;

import com.example.demo.domain.conversation.entity.ConversationSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ConversationResponseDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConversationStartResponseDto {
        private Long sessionId;
        private String nextStory;
        private ConversationSession.ConversationStep currentStep;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeedbackResponseDto {
        private String feedbackResult;  // "GOOD" or "NEEDS_CORRECTION"
        private String feedbackText;
        private ConversationSession.ConversationStep currentStep;
    }


}