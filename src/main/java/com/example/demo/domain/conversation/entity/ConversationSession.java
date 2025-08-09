package com.example.demo.domain.conversation.entity;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
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
@Table(name = "conversation_session_entity")
public class ConversationSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    // 현재 대화 단계 (기본값 START)
    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    private ConversationStep currentStep = ConversationStep.START;

    // 대화 세션 삭제 시 메시지도 삭제
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC") // 메시지 순서 유지를 위해 ID 기준으로 정렬
    private List<ConversationMessage> messages = new ArrayList<>();

    // 대화 세션이 속한 스토리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 대화 세션이 속한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum ConversationStep {
        START,   // 시작 (next_story만 있음)
        STEP_01, // 1단계
        STEP_02, // 2단계
        STEP_03, // 3단계
        END      // 종료
    }
}