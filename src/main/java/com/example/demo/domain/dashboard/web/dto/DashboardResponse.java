package com.example.demo.domain.dashboard.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Long dashboardId;

    private List<StatItem> backgroundStats;
    private List<StatItem> themeStats;

    @Data
    @Builder
    public static class StatItem {
        private String name;
        private Long count;
        private double percent;
    }

}