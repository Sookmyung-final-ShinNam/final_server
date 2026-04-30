package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.SlotDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotDefinitionRepository extends JpaRepository<SlotDefinition, Long> {
}