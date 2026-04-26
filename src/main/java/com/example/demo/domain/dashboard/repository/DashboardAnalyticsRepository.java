package com.example.demo.domain.dashboard.repository;

import com.example.demo.domain.conversation.entity.StepAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DashboardAnalyticsRepository extends JpaRepository<StepAttempt, Long> {

    /**
     * Story 기준으로 모든 StepAttempt 조회
     */
    @Query("""
        SELECT a
        FROM StepAttempt a
        JOIN FETCH a.step st
        JOIN FETCH st.session cs
        WHERE cs.story.id = :storyId
    """)
    List<StepAttempt> findAllAttemptsByStoryId(Long storyId);

}