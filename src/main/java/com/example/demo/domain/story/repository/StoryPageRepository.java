package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.StoryPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryPageRepository extends JpaRepository<StoryPage, Long> {
    Optional<StoryPage> findByStory_IdAndPageNumber(Long storyId, int pageNumber);
}