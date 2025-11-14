package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardThemeUsage;
import com.example.demo.domain.story.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardThemeUsageRepository extends JpaRepository<DashboardThemeUsage, Long> {

    // 특정 테마에 대한 사용 통계 조회
    Optional<DashboardThemeUsage> findByDashboardAndTheme(Dashboard dashboard, Theme theme);

}