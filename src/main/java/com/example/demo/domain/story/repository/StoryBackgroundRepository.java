package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryBackground;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryBackgroundRepository extends JpaRepository<StoryBackground, Long> {
}