package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
}