package com.example.demo.domain.dashboard.service.analyzer;

import com.example.demo.domain.dashboard.converter.DashboardConverter;
import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.dashboard.entity.DashboardBackgroundUsage;
import com.example.demo.domain.dashboard.entity.DashboardThemeUsage;
import com.example.demo.domain.dashboard.repository.DashboardBackgroundUsageRepository;
import com.example.demo.domain.dashboard.repository.DashboardThemeUsageRepository;
import com.example.demo.domain.story.entity.Story;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관심사 통계 처리 서비스 (배경, 테마)
 * N+1 문제 제거 : 대시보드 단위로 배치 조회 후 메모리에서 매칭
 */
@Service
@RequiredArgsConstructor
public class InterestAnalyzerServiceImpl implements DashboardAnalyzerService {

    private final DashboardBackgroundUsageRepository bgRepo;
    private final DashboardThemeUsageRepository themeRepo;
    private final DashboardConverter converter;

    @Override
    public void apply(Dashboard dashboard, Story story) {

        // 1️. 배경 통계 처리 (N+1 방지)

        // 대시보드에 존재하는 모든 배경 사용 기록 조회
        List<DashboardBackgroundUsage> existingBgUsages = bgRepo.findAllByDashboard(dashboard);

        // 배경 -> 사용 기록 맵으로 변환
        Map<Long, DashboardBackgroundUsage> bgUsageMap = existingBgUsages.stream()
                .collect(Collectors.toMap(
                        usage -> usage.getBackground().getId(),
                        usage -> usage
                ));

        // 스토리의 각 배경 처리
        story.getStoryBackgrounds().forEach(sb -> {
            Long bgId = sb.getBackground().getId();
            DashboardBackgroundUsage usage = bgUsageMap.get(bgId);
            usage = converter.toBackgroundUsage(dashboard, sb.getBackground(), usage);
            bgRepo.save(usage);
        });


        // 2️. 테마 통계 처리 (N+1 방지)

        // 대시보드에 존재하는 모든 테마 사용 기록 조회
        List<DashboardThemeUsage> existingThemeUsages = themeRepo.findAllByDashboard(dashboard);

        // 테마 -> 사용 기록 맵으로 변환
        Map<Long, DashboardThemeUsage> themeUsageMap = existingThemeUsages.stream()
                .collect(Collectors.toMap(
                        usage -> usage.getTheme().getId(),
                        usage -> usage
                ));

        // 스토리의 각 테마 처리
        story.getStoryThemes().forEach(st -> {
            Long themeId = st.getTheme().getId();
            DashboardThemeUsage usage = themeUsageMap.get(themeId);
            usage = converter.toThemeUsage(dashboard, st.getTheme(), usage);
            themeRepo.save(usage);
        });
    }

}