package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardBackgroundUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DashboardBackgroundUsageRepository extends JpaRepository<DashboardBackgroundUsage, Long> {

    // N+1 방지용 : Dashboard 단위로 한번에 조회
    List<DashboardBackgroundUsage> findAllByDashboard(Dashboard dashboard);

}