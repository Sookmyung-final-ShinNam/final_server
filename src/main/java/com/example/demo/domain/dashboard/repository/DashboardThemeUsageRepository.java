package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardThemeUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardThemeUsageRepository extends JpaRepository<DashboardThemeUsage, Long> {

    // N+1 방지용 : Dashboard 단위로 한번에 조회
    List<DashboardThemeUsage> findAllByDashboard(Dashboard dashboard);

}