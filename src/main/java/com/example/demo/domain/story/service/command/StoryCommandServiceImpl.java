package com.example.demo.domain.story.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.event.CompleteConversationEvent;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class StoryCommandServiceImpl implements StoryCommandService {

    private final StoryRepository storyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void markStoryVideoAsMaking(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 유저 포인트 사용
        story.getUser().usePoints(1);

        story.markVideoAsMaking();  // 엔티티 내부에서 상태 변경
        storyRepository.save(story); // DB 반영
    }

    @Override
    @Transactional
    public void retryFailedStories(Long storyId, Long sessionId) {

        // 1. Story 조회 및 재생성 횟수 업데이트
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        story.setRetryCount(story.getRetryCount() + 1);

        // 2. 스토리 생성 이벤트 발행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(
                                new CompleteConversationEvent(storyId, sessionId)
                        );
                    }
                }
        );
    }
}