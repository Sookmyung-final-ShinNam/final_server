package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionStepRepository extends JpaRepository<SessionStep, Long> {

    // sessionId + stepType으로 특정 Step 조회
    Optional<SessionStep> findBySessionIdAndStepType(
            Long sessionId,
            ConversationSession.ConversationStep stepType
    );

    Optional<SessionStep> findBySessionAndStepType(
            ConversationSession session,
            ConversationSession.ConversationStep stepType
    );

}