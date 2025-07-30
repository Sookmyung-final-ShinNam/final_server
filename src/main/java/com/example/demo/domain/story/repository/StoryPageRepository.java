package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryPageRepository extends JpaRepository<StoryPage, Long> {
}