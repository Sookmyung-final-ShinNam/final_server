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

    @Column(name = "llm_question", nullable = false, columnDefinition = "TEXT")
    private String llmQuestion;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private SessionStep step;
}