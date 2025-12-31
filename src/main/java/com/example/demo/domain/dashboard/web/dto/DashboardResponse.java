package com.example.demo.domain.dashboard.web.dto;

import com.example.demo.domain.dashboard.entity.FeedbackAttemptStats;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private String username;
    private Long dashboardId;

    // 최대 관심사
    private String maxTheme;
    private String maxBackground;

    // 관심사 통계
    private List<InterestStatItem> backgroundStats;
    private List<InterestStatItem> themeStats;

    // 언어 통계
    private List<LanguageStatItem> languageStats;

    // 정서 통계
    private List<EmotionStatItem> emotionsStats;

    // 부모님 조언
    private String parentAdvice;

    @Data
    @Builder
    public static class InterestStatItem {
        private String name;
        private Long count;
        private double percent;
    }

    @Data
    @Builder
    public static class LanguageStatItem {
        private Long storyId;
        private String storyName;
        private LocalDateTime createdAt;
        private FeedbackAttemptStats attemptStats;
        private double avgAttemptPerStage;
        private int avgAnswerLength;
        private List<String> newWords;
    }

    @Data
    @Builder
    public static class EmotionStatItem {
        private Long storyId;
        private LocalDateTime createdAt;
        private double joy;
        private double sadness;
        private double anger;
        private double fear;
        private double surprise;
        private double neutral;
        private String summary;
    }

}