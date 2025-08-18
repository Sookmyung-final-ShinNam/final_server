package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryBackground;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryBackgroundRepository extends JpaRepository<StoryBackground, Long> {
}