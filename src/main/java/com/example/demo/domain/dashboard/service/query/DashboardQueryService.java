package com.example.demo.domain.dashboard.service.query;

import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.user.entity.User;

public interface DashboardQueryService {

    /**
     * 현재 유저 대시보드 조회
     */
    DashboardResponse getDashboard(User user);

}