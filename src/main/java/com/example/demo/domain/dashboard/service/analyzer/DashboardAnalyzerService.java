package com.example.demo.domain.dashboard.service.analyzer;

import com.example.demo.domain.dashboard.entity.Dashboard;
import com.example.demo.domain.story.entity.Story;

public interface DashboardAnalyzerService {

    /**
     * 스토리 데이터를 이용해 대시보드 통계를 업데이트
     */
    void apply(Dashboard dashboard, Story story);

}