package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.StepSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepSlotRepository extends JpaRepository<StepSlot, Long> {
}