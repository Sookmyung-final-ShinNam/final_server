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
        private int tryNum;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepMessageResponseDto {
        private Long messageId;
        private String nextStory;
        private String llmQuestion;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepPendingResponseDto {
        private String status; // "PENDING"
    }

    // 이어하기: 진행 중(끝내지 않은) 대화 세션
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InProgressSessionDto {
        private Long sessionId;
        private ConversationSession.ConversationStep currentStep; // 이어서 시작할 단계
        private String characterName;
        private Long assignmentId; // 학급 과제로 만든 경우에만 값이 있음 (일반 동화는 null)
        private Long classroomId;  // 학급 과제로 만든 경우에만 값이 있음 (일반 동화는 null)
    }

}