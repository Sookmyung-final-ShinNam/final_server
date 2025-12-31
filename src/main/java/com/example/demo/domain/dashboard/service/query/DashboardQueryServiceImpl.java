package com.example.demo.domain.dashboard.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.dashboard.converter.DashboardConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardBackgroundUsage;
import com.example.demo.domain.dashboard.entity.DashboardStoryStats;
import com.example.demo.domain.dashboard.entity.DashboardThemeUsage;
import com.example.demo.domain.dashboard.repository.DashboardBackgroundUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardStoryStatsRepository;
import com.example.demo.domain.dashboard.repository.DashboardThemeUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardRepository;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final DashboardRepository dashboardRepository;
    private final DashboardBackgroundUsageRepository bgRepo;
    private final DashboardThemeUsageRepository themeRepo;
    private final DashboardStoryStatsRepository storyStatsRepo;
    private final StoryRepository storyRepo;
    private final DashboardConverter converter;

    @Override
    public DashboardResponse getDashboard(User user) {
        Dashboard dashboard = dashboardRepository.findByUser(user).orElse(null); // null 가능

        // -- dashboard null 처리 --
        // 배경 전체
        List<DashboardBackgroundUsage> bgUsages =
                dashboard != null ?
                        bgRepo.findAllByDashboard(dashboard) : List.of();
        // 테마 전체
        List<DashboardThemeUsage> themeUsages =
                dashboard != null ?
                        themeRepo.findAllByDashboard(dashboard) : List.of();

        // 최근 10개 StoryStats만 조회
        List<DashboardStoryStats> storyStats =
                dashboard != null ?
                        storyStatsRepo.findTop10ByDashboardOrderByCreatedAtDesc(dashboard) : List.of();

        // storyId - storyTitle 조회 및 매핑
        Map<Long, String> storyTitles = Map.of();
        if (!storyStats.isEmpty()) {
            List<Long> storyIds = storyStats.stream()
                    .map(DashboardStoryStats::getStoryId)
                    .distinct()
                    .toList();
            storyTitles  = storyRepo.findAllById(storyIds).stream()
                            .collect(toMap(Story::getId, Story::getTitle));
        }

        return converter.toResponse(
                user.getNickname(),
                dashboard, // null 가능
                bgUsages,
                themeUsages,
                storyStats,
                storyTitles
        );
    }
}