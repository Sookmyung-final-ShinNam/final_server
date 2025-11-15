package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.entity.*;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DashboardConverter {

    private final DashboardInterestConverter interestConverter;
    private final DashboardLanguageConverter languageConverter;
    private final DashboardEmotionConverter emotionConverter;

    public DashboardResponse toResponse(
            Dashboard dashboard,
            List<DashboardBackgroundUsage> bgUsages, List<DashboardThemeUsage> themeUsages, // 관심사 통계
            List<DashboardStoryStats> storyStats // 스토리 통계
    ) {
        return DashboardResponse.builder()
                .dashboardId(dashboard.getId())
                .backgroundStats(interestConverter.toBackgroundStats(bgUsages))
                .themeStats(interestConverter.toThemeStats(themeUsages))
                .languageStats(languageConverter.toLanguageStats(storyStats))
                .emotionsStats(emotionConverter.toEmotionStats(storyStats))
                .parentAdvice(dashboard.getParentAdvice())
                .build();
    }

}