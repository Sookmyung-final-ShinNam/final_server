package com.example.demo.domain.story.web.dto;

import com.example.demo.domain.conversation.entity.ConversationSession;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponseDto {

    // 내부 스토리 DTO
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoryItem {
        private Long storyId;
        private String title;
        private String description;
        private boolean completed;      // 완료 여부
        private boolean canContinue;    // 미완성이면 이어하기 가능
        private Long sessionId;         // 이어하기 가능한 경우 -> // 세션 ID
        private ConversationSession.ConversationStep currentStep; // 이어하기 가능한 경우 -> 현재 단계
        private boolean favorite;       // 관심 동화 여부
        private String imageUrl;        // 첫 장 이미지 URL
    }

    // 페이징 응답 DTO
    private List<StoryItem> stories;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

}