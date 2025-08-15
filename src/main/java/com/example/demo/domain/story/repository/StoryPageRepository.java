package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryPageRepository extends JpaRepository<StoryPage, Long> {
}