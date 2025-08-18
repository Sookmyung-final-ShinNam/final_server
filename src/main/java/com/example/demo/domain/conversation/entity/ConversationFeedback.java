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
@Table(name = "conversation_feedback_entity")
public class ConversationFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    // 시도 횟수 (1~3 허용)
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    // 사용자 답변
    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    // 정답 여부 (기본값 false)
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    // 피드백 텍스트
    @Column(name = "feedback_text", nullable = false, columnDefinition = "TEXT")
    private String feedbackText;

    // 피드백이 속한 메시지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ConversationMessage message;
}