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
@Table(name = "conversation_session")
public class ConversationSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    public enum ConversationStep {
        START,   // 시작
        기,
        승,
        전,
        결,
        END      // 종료
    }
    @Column(name = "current_step", nullable = false)
    private ConversationStep currentStep; 

    @Column(name = "state", nullable = false)
    private String state; // ACTIVE, COMPLETED

    @Column(name = "full_story", columnDefinition = "TEXT")
    private String fullStory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionStep> steps = new ArrayList<>();
}