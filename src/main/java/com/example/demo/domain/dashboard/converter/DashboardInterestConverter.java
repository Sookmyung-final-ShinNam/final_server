package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.entity.*;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.story.entity.Background;
import com.example.demo.domain.story.entity.Theme;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DashboardInterestConverter {

    /** 배경 통계 변환 */
    public List<DashboardResponse.InterestStatItem> toBackgroundStats(List<DashboardBackgroundUsage> bgUsages) {
        long total = bgUsages.stream().mapToLong(DashboardBackgroundUsage::getCount).sum();

        return bgUsages.stream()
                .map(u -> DashboardResponse.InterestStatItem.builder()
                        .name(u.getBackground().getName())
                        .count(u.getCount())
                        .percent(total == 0 ? 0 : (u.getCount() * 100.0 / total))
                        .build())
                .toList();
    }

    /** 테마 통계 변환 */
    public List<DashboardResponse.InterestStatItem> toThemeStats(List<DashboardThemeUsage> themeUsages) {
        long total = themeUsages.stream().mapToLong(DashboardThemeUsage::getCount).sum();

        return themeUsages.stream()
                .map(u -> DashboardResponse.InterestStatItem.builder()
                        .name(u.getTheme().getName())
                        .count(u.getCount())
                        .percent(total == 0 ? 0 : (u.getCount() * 100.0 / total))
                        .build())
                .toList();
    }

    /** 배경 사용 통계 생성/갱신 */
    public DashboardBackgroundUsage toBackgroundUsage(
            Dashboard dashboard,
            Background background,
            DashboardBackgroundUsage current
    ) {
        if (current == null) {
            return DashboardBackgroundUsage.builder()
                    .dashboard(dashboard)
                    .background(background)
                    .count(1L)
                    .build();
        }
        current.setCount(current.getCount() + 1);
        return current;
    }

    /** 테마 사용 통계 생성/갱신 */
    public DashboardThemeUsage toThemeUsage(
            Dashboard dashboard,
            Theme theme,
            DashboardThemeUsage current
    ) {
        if (current == null) {
            return DashboardThemeUsage.builder()
                    .dashboard(dashboard)
                    .theme(theme)
                    .count(1L)
                    .build();
        }
        current.setCount(current.getCount() + 1);
        return current;
    }

}