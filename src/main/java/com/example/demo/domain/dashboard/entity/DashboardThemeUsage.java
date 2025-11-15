package com.example.demo.domain.dashboard.entity;

import com.example.demo.domain.story.entity.Theme;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard_theme_usage")
public class DashboardThemeUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_usage_id")
    private Long id;

    // 어떤 대시보드의 통계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    // 어떤 테마에 대한 통계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    // 해당 테마가 사용된 총 횟수
    private Long count;

}