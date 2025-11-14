package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardBackgroundUsage;
import com.example.demo.domain.story.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardBackgroundUsageRepository extends JpaRepository<DashboardBackgroundUsage, Long> {

    // 특정 배경에 대한 사용 통계 조회
    Optional<DashboardBackgroundUsage> findByDashboardAndBackground(Dashboard dashboard, Background background);

}