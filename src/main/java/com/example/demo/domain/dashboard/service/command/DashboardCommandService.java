package com.example.demo.domain.dashboard.service.command;

import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.user.entity.User;

/**
 * 대시보드 업데이트 서비스
 * - 여러 AnalyzerService 구현체를 주입받아 순차적으로 실행
 */
public interface DashboardCommandService {

    Long updateByStory(Long storyId, User user);
}