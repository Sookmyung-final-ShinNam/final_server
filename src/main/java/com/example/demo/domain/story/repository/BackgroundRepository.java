package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackgroundRepository extends JpaRepository<Background, Long> {
}