package com.example.demo.domain.dashboard.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.dashboard.converter.DashboardConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.repository.DashboardBackgroundUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardStoryStatsRepository;
import com.example.demo.domain.dashboard.repository.DashboardThemeUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardRepository;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final DashboardRepository dashboardRepository;
    private final DashboardBackgroundUsageRepository bgRepo;
    private final DashboardThemeUsageRepository themeRepo;
    private final DashboardStoryStatsRepository storyStatsRepo;
    private final DashboardConverter converter;

    @Override
    public DashboardResponse getDashboard(User user) {
        Dashboard dashboard = dashboardRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorStatus.DASHBOARD_NOT_FOUND));

        return converter.toResponse(
                dashboard,
                bgRepo.findAllByDashboard(dashboard),
                themeRepo.findAllByDashboard(dashboard),
                storyStatsRepo.findTop10ByDashboardOrderByCreatedAtDesc(dashboard) // 최근 10개 StoryStats만 조회
        );
    }

}