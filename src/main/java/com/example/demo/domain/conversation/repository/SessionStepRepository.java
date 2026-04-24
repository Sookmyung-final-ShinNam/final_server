package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.SessionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionStepRepository extends JpaRepository<SessionStep, Long> {
}