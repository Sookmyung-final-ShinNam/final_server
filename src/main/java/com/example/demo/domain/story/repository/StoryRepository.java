package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findByStorySessions_Id(Long sessionId);
    Page<Story> findAllByUser(User user, Pageable pageable);
}