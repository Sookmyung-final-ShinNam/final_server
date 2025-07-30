package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {
}