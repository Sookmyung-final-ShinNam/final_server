package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardEmotionConverter {

    /** 감정 통계 변환 */
    public List<DashboardResponse.EmotionStatItem> toEmotionStats(List<DashboardStoryStats> storyStats) {
        return storyStats.stream()
                .map(s -> DashboardResponse.EmotionStatItem.builder()
                        .storyId(s.getStoryId())
                        .createdAt(s.getUpdatedAt())
                        .joy(s.getJoy())
                        .sadness(s.getSadness())
                        .anger(s.getAnger())
                        .fear(s.getFear())
                        .surprise(s.getSurprise())
                        .neutral(s.getNeutral())
                        .summary(s.getSummary())
                        .build())
                .collect(Collectors.toList());
    }

}