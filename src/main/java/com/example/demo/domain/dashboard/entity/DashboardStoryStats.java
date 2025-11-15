package com.example.demo.domain.dashboard.entity;

import com.example.demo.global.converter.StringListConverter;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard_story_stats")
public class DashboardStoryStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dashboard_story_stats_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    // 기/승/전/결 단계별 시도 횟수
    @Embedded
    private FeedbackAttemptStats feedbackAttemptStats;

    // 평균 시도 횟수 : (기+승+전+결) / 4
    @Column(name = "avg_attempt_per_stage")
    private Double avgAttemptPerStage;

    // user_answer 평균 길이
    @Column(name = "avg_answer_length")
    private Integer avgAnswerLength;

    // 새 단어
    @Convert(converter = StringListConverter.class)
    @Column(name = "new_words", columnDefinition = "TEXT")
    private List<String> newWords;

}