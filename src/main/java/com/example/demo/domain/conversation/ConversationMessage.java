package com.example.demo.domain.conversation;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversation_message_entity")
@Getter
@Setter
@NoArgsConstructor
public class ConversationMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // LLM의 질문
    @Column(name = "llm_question", columnDefinition = "TEXT")
    private String llmQuestion;

    // LLM의 답변
    @Column(name = "llm_answer", columnDefinition = "TEXT")
    private String llmAnswer;

    // 다음 스토리 내용
    @Column(name = "next_story", columnDefinition = "TEXT")
    private String nextStory;

    // 메시지 삭제 시 피드백도 삭제
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("attemptNumber ASC") // 시도 횟수 기준으로 정렬
    private Set<ConversationFeedback> feedbacks = new HashSet<>();

    // 메시지가 속한 대화 세션
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;
}