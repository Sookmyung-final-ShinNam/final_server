package com.example.demo.domain.story.scheduler;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.service.command.StoryCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryRetryScheduler {

    final private ConversationSessionRepository sessionRepo;
    final private StoryCommandService storyCommandService;

    /**
     * 매일 자정 스토리 재생성 배치 시작
     * - 실패 및 미완료 스토리 대상으로 스토리 생성 이벤트(CompleteConversationEvent) 발행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void retryScheduledStories() {
        log.info("[BATCH] 실패 및 오래된 스토리 재생성 배치 시작");

        // 1. 스토리 재생성 배치 대상 세션 조회
        List<ConversationSession> targetSessions = sessionRepo.findRetryTargetSessions(
                ConversationSession.ConversationStep.END,
                List.of(
                        Story.StoryStatus.TEXT_FAILED,
                        Story.StoryStatus.IMAGE_FAILED,
                        Story.StoryStatus.VIDEO_FAILED
                ),
                List.of(
                        Story.StoryStatus.MAKING,
                        Story.StoryStatus.TEXT_COMPLETED
                ),
                LocalDateTime.now().minusMinutes(30)
        );

        // 2. 스토리 재생성 이벤트 발행 (+ retry_count 증가)
        for (ConversationSession session : targetSessions) {
            Story story = session.getStory();

            storyCommandService.retryFailedStories(story.getId(), session.getId()); // 횟수 증가 및 이벤트 발행
            log.info("[BATCH] 스토리 재생성 이벤트 발행 storyId={}, sessionId={}", story.getId(), session.getId());
        }

        log.info("[BATCH] 실패 및 오래된 스토리 재생성 배치 종료: 재생성 대상 개수={}", targetSessions.size());
    }
}
