package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findByStorySessions_Id(Long sessionId);

    // 특정 사용자가 작성한 스토리 조회
    List<Story> findByUser(User user);
}