package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationFeedbackRepository extends JpaRepository<ConversationFeedback, Long> {
}