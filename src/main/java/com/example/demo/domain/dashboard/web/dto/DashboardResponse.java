package com.example.demo.domain.dashboard.web.dto;

import com.example.demo.domain.dashboard.entity.FeedbackAttemptStats;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Long dashboardId;

    // 관심사 통계
    private List<InterestStatItem> backgroundStats;
    private List<InterestStatItem> themeStats;

    // 언어 통계
    private List<LanguageStatItem> languageStats;

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
        private LocalDateTime createdAt;
        private FeedbackAttemptStats attemptStats;
        private double avgAttemptPerStage;
        private int avgAnswerLength;
        private List<String> newWords;
    }

}