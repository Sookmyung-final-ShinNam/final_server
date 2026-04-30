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

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private ConversationSession.ConversationStep stepType; // START와 END는 미사용

    public enum Status {
        NONE,
        IN_PROGRESS,
        COMPLETED
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStep.Status status;

    @Column(name = "prev_context", columnDefinition = "TEXT")
    private String prevContext; // 이전 맥락 -> llm 호출시 상황 전달용

    @Column(name = "next_story", columnDefinition = "TEXT")
    private String nextStory; // 다음 이야기

    @Column(name = "llm_question", nullable = true, columnDefinition = "TEXT")
    private String llmQuestion; // 질문

    @Column(name = "final_answer", columnDefinition = "TEXT")
    private String finalAnswer; // 질문에 대한 대답 - 성공한 피드백을 저장

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