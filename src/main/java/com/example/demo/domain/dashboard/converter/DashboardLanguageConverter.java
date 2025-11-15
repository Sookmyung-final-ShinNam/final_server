package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.entity.FeedbackAttemptStats;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Component
public class DashboardLanguageConverter {

    /**
     * FeedbackAttemptStats 생성
     */
    public FeedbackAttemptStats toAttemptStats(
            int gi, int seung, int jeon, int gyeol
    ) {
        return FeedbackAttemptStats.builder()
                .giCount(gi)
                .seungCount(seung)
                .jeonCount(jeon)
                .gyeolCount(gyeol)
                .build();
    }

    /**
     * DashboardStoryStats 생성
     */
    public DashboardStoryStats toStoryStats(
            Dashboard dashboard,
            Long storyId,
            FeedbackAttemptStats attemptStats,
            double avgAttemptPerStage,
            int avgAnswerLength,
            List<String> newWords
    ) {
        return DashboardStoryStats.builder()
                .dashboard(dashboard)
                .storyId(storyId)
                .feedbackAttemptStats(attemptStats)
                .avgAttemptPerStage(avgAttemptPerStage)
                .avgAnswerLength(avgAnswerLength)
                .newWords(newWords)
                .build();
    }

    /** 언어 통계 변환 */
    public List<DashboardResponse.LanguageStatItem> toLanguageStats(List<DashboardStoryStats> stats) {
        return stats.stream()
                .map(s -> DashboardResponse.LanguageStatItem.builder()
                        .storyId(s.getStoryId())
                        .createdAt(s.getUpdatedAt())
                        .attemptStats(s.getFeedbackAttemptStats())
                        .avgAttemptPerStage(
                                s.getAvgAttemptPerStage() != null ? s.getAvgAttemptPerStage() : 0.0
                        )
                        .avgAnswerLength(
                                s.getAvgAnswerLength() != null ? s.getAvgAnswerLength() : 0
                        )
                        .newWords(
                                Optional.ofNullable(s.getNewWords()).orElse(Collections.emptyList())
                        )
                        .build())
                .toList();
    }

}