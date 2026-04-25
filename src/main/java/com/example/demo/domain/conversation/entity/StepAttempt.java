package com.example.demo.domain.conversation.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "step_attempt")
public class StepAttempt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long id;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo; // 1~3

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer; // 사용자 대답 - raw data

    @Column(name = "is_correct")
    private Boolean isCorrect; // 정답 여부

    @Column(name = "llm_feedback", columnDefinition = "TEXT")
    private String llmFeedback; // LLM 피드백

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private SessionStep step;
}