package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardStoryStatsRepository extends JpaRepository<DashboardStoryStats, Long> {

    /**
     * 특정 Dashboard에 연결된 모든 Story 통계 조회
     */
    List<DashboardStoryStats> findAllByDashboard(Dashboard dashboard);

    /**
     * 특정 Dashboard와 Story ID로 스토리 통계 조회
     */
    Optional<DashboardStoryStats> findByDashboardAndStoryId(Dashboard dashboard, Long storyId);

    /**
     * 특정 유저의 최근 5개 스토리 통계 조회 (최신순)
     */
    List<DashboardStoryStats> findTop5ByDashboard_User_IdOrderByCreatedAtDesc(Long userId);

}