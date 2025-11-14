package com.example.demo.domain.dashboard.converter;

import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.dashboard.entity.DashboardBackgroundUsage;
import com.example.demo.domain.dashboard.entity.DashboardThemeUsage;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.story.entity.Background;
import com.example.demo.domain.story.entity.Theme;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DashboardConverter {

    /**
     * Dashboard 엔티티와 사용 통계 데이터를 DTO로 변환
     */
    public DashboardResponse toResponse(
            Dashboard dashboard,
            List<DashboardBackgroundUsage> bgUsages,   // 배경 사용 통계
            List<DashboardThemeUsage> themeUsages      // 테마 사용 통계
    ) {

        // 배경과 테마의 총 사용 횟수 계산
        long bgTotal = bgUsages.stream().mapToLong(DashboardBackgroundUsage::getCount).sum();
        long themeTotal = themeUsages.stream().mapToLong(DashboardThemeUsage::getCount).sum();

        // 배경별 사용 통계 생성 (이름, 사용 횟수, 비율)
        List<DashboardResponse.StatItem> bgStats = bgUsages.stream()
                .map(u -> DashboardResponse.StatItem.builder()
                        .name(u.getBackground().getName())  // 배경 이름
                        .count(u.getCount())               // 사용 횟수
                        .percent(bgTotal == 0 ? 0 : (u.getCount() * 100.0 / bgTotal))  // 사용 비율
                        .build()
                )
                .toList();

        // 테마별 사용 통계 생성 (이름, 사용 횟수, 비율)
        List<DashboardResponse.StatItem> themeStats = themeUsages.stream()
                .map(u -> DashboardResponse.StatItem.builder()
                        .name(u.getTheme().getName())       // 테마 이름
                        .count(u.getCount())               // 사용 횟수
                        .percent(themeTotal == 0 ? 0 : (u.getCount() * 100.0 / themeTotal))  // 사용 비율
                        .build()
                )
                .toList();

        // 최종 DashboardResponse DTO 생성
        return DashboardResponse.builder()
                .dashboardId(dashboard.getId())      // 대시보드 ID
                .backgroundStats(bgStats)            // 배경 통계
                .themeStats(themeStats)              // 테마 통계
                .build();
    }

    /**
     * 배경 사용 통계 객체 생성 또는 갱신
     */
    public DashboardBackgroundUsage toBackgroundUsage(Dashboard dashboard, Background background, DashboardBackgroundUsage currentUsage) {
        if (currentUsage == null) {
            return DashboardBackgroundUsage.builder()
                    .dashboard(dashboard)
                    .background(background)
                    .count(1L) // 새로 생성되면 1로 초기화
                    .build();
        } else {
            currentUsage.setCount(currentUsage.getCount() + 1); // 기존 객체면 카운트 증가
            return currentUsage;
        }
    }

    /**
     * 테마 사용 통계 객체 생성 또는 갱신
     */
    public DashboardThemeUsage toThemeUsage(Dashboard dashboard, Theme theme, DashboardThemeUsage currentUsage) {
        if (currentUsage == null) {
            return DashboardThemeUsage.builder()
                    .dashboard(dashboard)
                    .theme(theme)
                    .count(1L) // 새로 생성되면 1로 초기화
                    .build();
        } else {
            currentUsage.setCount(currentUsage.getCount() + 1); // 기존 객체면 카운트 증가
            return currentUsage;
        }
    }

}