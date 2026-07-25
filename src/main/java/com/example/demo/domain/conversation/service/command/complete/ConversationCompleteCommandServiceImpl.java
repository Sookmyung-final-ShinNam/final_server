package com.example.demo.domain.conversation.service.command.complete;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.*;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCompleteCommandServiceImpl implements ConversationCompleteCommandService {

    private final StoryPageRepository storyPageRepo;
    private final StoryRepository storyRepo;
    private final ConversationSessionRepository sessionRepo;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void completeStory(Long sessionId) {

        // 1. Story 및 Session 조회
        Story story = storyRepo.findByStorySessions_Id(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 현재 대화 세션 단계가 END 이고 상태가 COMPLETED 인지 확인
        if (session.getCurrentStep() != ConversationSession.ConversationStep.END ||
            session.getState() != ConversationSession.SessionState.COMPLETED
        ) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 3. 스토리 상태 변경 MAKING 에서는 이어하기 불가
        if (story.getStoryStatus() == Story.StoryStatus.IN_PROGRESS) {
            story.setStoryStatus(Story.StoryStatus.MAKING);
        }

        // 4. 커밋 이후 이벤트 발행: 비동기 작업 (동화 생성) 시작
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(
                                new CompleteConversationEvent(story.getId(), sessionId)
                        );
                    }
                }
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailedStory(Long storyId, Story.StoryStatus failedStatus) {

        // 1. 실패 상테 해당하는 enum 값 검증
        if (!failedStatus.isFailedStory()) {
            throw new CustomException(ErrorStatus.STORY_INVALID_STATUS);
        }

        // 2. 실패 상태 업데이트
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        story.setStoryStatus(failedStatus);
    }

    @Override
    @Transactional
    public void updateFailedVideo(Long pageId, Long storyId) {

        // 1. 엔티티 조회
        StoryPage page = storyPageRepo.findById(pageId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND));

        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));


        // 2. 동영상 페이지 실패 시,바로 스토리 실패 상태 업데이트
        page.setVideoStatus(StoryPage.VideoStatus.FAILED);
        story.setVideoStatus(Story.VideoStatus.VIDEO_FAILED);
    }

    @Override
    @Transactional
    public void retryFailedStories(Long storyId, Long sessionId) {

        // 1. Story 조회 및 재생성 횟수 업데이트
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        story.setRetryCount(story.getRetryCount() + 1);

        // 2. 스토리 생성 이벤트 발행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(
                                new RetryStoryEvent(storyId, sessionId)
                        );
                    }
                }
        );
    }
}