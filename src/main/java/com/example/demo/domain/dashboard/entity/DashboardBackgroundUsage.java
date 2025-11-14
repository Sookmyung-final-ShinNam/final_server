package com.example.demo.domain.dashboard.entity;

import com.example.demo.domain.story.entity.Background;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard_background_usage")
public class DashboardBackgroundUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bg_usage_id")
    private Long id;

    // 어떤 대시보드의 통계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    // 어떤 배경에 대한 통계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "background_id", nullable = false)
    private Background background;

    // 해당 배경이 사용된 총 횟수
    private Long count;

}