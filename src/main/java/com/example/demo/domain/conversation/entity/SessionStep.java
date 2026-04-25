package com.example.demo.domain.conversation.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "session_step",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "step_type"})
        }
)
public class SessionStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long id;

    @Column(name = "step_type", nullable = false)
    private String stepType; // 기, 승, 전, 결

    @Column(name = "status", nullable = false)
    private String status; // NONE, IN_PROGRESS, COMPLETED

    @Column(name = "prev_context", columnDefinition = "TEXT")
    private String prevContext; // 이전 맥락 (prevContext + finalAnswer + nextStory) -> llm 호출시 상황 전달용

    @Column(name = "next_story", columnDefinition = "TEXT")
    private String nextStory; // 다음 이야기

    @Column(name = "llm_question", nullable = false, columnDefinition = "TEXT")
    private String llmQuestion; // 질문

    @Column(name = "final_answer", columnDefinition = "TEXT")
    private String finalAnswer; // 질문에 대한 대답 - 성공한 피드백을 저장 .

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @Builder.Default
    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepAttempt> attempts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepSlot> slots = new ArrayList<>();
}