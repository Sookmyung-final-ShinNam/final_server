package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryTheme;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface StoryThemeRepository extends JpaRepository<StoryTheme, Long> {
}