package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.entity.*;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DashboardConverter {

    private final DashboardInterestConverter interestConverter;
    private final DashboardLanguageConverter languageConverter;
    private final DashboardEmotionConverter emotionConverter;

    public DashboardResponse toResponse(
            String username,
            Dashboard dashboard,
            List<DashboardBackgroundUsage> bgUsages, List<DashboardThemeUsage> themeUsages, // 관심사 통계
            List<DashboardStoryStats> storyStats, // 스토리 통계
            Map<Long, String> storyTitles         // 스토리 아이디 및 제목
    ) {
        return DashboardResponse.builder()
                .username(username)
                .dashboardId(dashboard != null ? dashboard.getId() : null)
                .maxBackground(interestConverter.toMaxBackground(bgUsages))
                .maxTheme(interestConverter.toMaxTheme(themeUsages))
                .backgroundStats(interestConverter.toBackgroundStats(bgUsages))
                .themeStats(interestConverter.toThemeStats(themeUsages))
                .languageStats(languageConverter.toLanguageStats(storyStats, storyTitles))
                .emotionsStats(emotionConverter.toEmotionStats(storyStats))
                .parentAdvice(dashboard != null ? dashboard.getParentAdvice() : null)
                .build();
    }
}