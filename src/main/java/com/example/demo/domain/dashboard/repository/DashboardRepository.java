package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    // user 로 대시보드 조회
    Optional<Dashboard> findByUser(User user);

}