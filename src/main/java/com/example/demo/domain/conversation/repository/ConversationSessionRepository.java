package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    // User 기준으로 모든 세션 삭제
    void deleteAllByUser(User user);
}