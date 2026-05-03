package com.example.demo.domain.conversation.repository;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    // User 기준으로 모든 세션 삭제
    void deleteAllByUser(User user);

    /**
     * 스토리 재생성 배치 대상 세션 조회
     *
     * 배치 대상 스토리 기준:
     * - 대화 세션이 완료되었고,
     * - 스토리의 상태가 패한 TEXT_FAILED/IMAGE_FAILED/VIDEO_FAILED 또는 오래된 MAKING/TEXT_COMPLETED 이고,
     * - 스토리의 재생성 쵯수가 3번 미만
     */
    @Query("""
        SELECT cs 
        FROM ConversationSession cs
        JOIN FETCH cs.story s
            WHERE cs.currentStep = :step
                AND (
                    s.storyStatus IN :fail
                    OR (s.storyStatus IN :stale AND s.updatedAt < :threshold)
                    )
                AND s.retryCount < 3
    """)
    List<ConversationSession> findRetryTargetSessions(
            @Param("step") ConversationSession.ConversationStep step,
            @Param("fail") List<Story.StoryStatus> failStatus,
            @Param("stale") List<Story.StoryStatus> staleStatus,
            @Param("threshold") LocalDateTime threshold
    );
}