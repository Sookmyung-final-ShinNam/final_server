package com.example.demo.domain.dashboard.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.dashboard.converter.DashboardConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.repository.DashboardBackgroundUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardStoryStatsRepository;
import com.example.demo.domain.dashboard.repository.DashboardThemeUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardRepository;
import com.example.demo.domain.dashboard.service.analyzer.DashboardAnalyzerService;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardCommandServiceImpl implements DashboardCommandService {

    private final DashboardRepository dashboardRepository;
    private final StoryRepository storyRepository;
    private final List<DashboardAnalyzerService> analyzers; // 모든 Analyzer 구현체 DI
    private final DashboardConverter converter;

    private final DashboardBackgroundUsageRepository bgRepo;
    private final DashboardThemeUsageRepository themeRepo;
    private final DashboardStoryStatsRepository storyStatsRepo;

    /**
     * 스토리 기반으로 대시보드 업데이트
     * N+1 문제 방지를 위해 배경/테마 통계를 배치 조회
     */
    @Transactional
    public Long updateByStory(Long storyId, User user) {

        // 1️. 사용자의 대시보드 조회, 없으면 새로 생성
        Dashboard dashboard = dashboardRepository.findByUser(user)
                .orElseGet(() -> dashboardRepository.save(Dashboard.builder().user(user).build()));

        // 2️. 스토리 조회
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 3️. 이미 반영된 스토리는 중복 방지
        if (story.isDashboardApplied()) {
            throw new CustomException(ErrorStatus.DASHBOARD_ALREADY_APPLIED);
        }

        // 4️. 모든 분석 서비스 적용
        analyzers.forEach(analyzer -> analyzer.apply(dashboard, story));

        // 5️. 반영 완료 표시
        story.setDashboardApplied(true);

        // 6. DTO 변환
        return dashboard.getId();
    }

}