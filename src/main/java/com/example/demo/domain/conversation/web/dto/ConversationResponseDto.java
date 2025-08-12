package com.example.demo.domain.conversation.web.dto;

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
    }


}