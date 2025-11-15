package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardStoryStatsRepository extends JpaRepository<DashboardStoryStats, Long> {

    /**
     * 특정 Dashboard에 연결된 모든 Story 통계 조회
     */
    List<DashboardStoryStats> findAllByDashboard(Dashboard dashboard);

}