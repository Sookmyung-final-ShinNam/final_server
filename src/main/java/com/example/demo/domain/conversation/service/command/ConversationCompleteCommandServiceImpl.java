package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.*;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
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

        // 2. 현재 대화 단계가 END 인지 확인
        if (session.getCurrentStep() != ConversationSession.ConversationStep.END) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }


        // 3. 스토리 상태 변경 MAKING 에서는 이어하기 불가
        if (story.getStatus() == Story.StoryStatus.IN_PROGRESS) {
            story.setStatus(Story.StoryStatus.MAKING);
        }

        // 5. 커밋 이후 이벤트 발행: 비동기 작업 (동화 생성) 시작
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
    public void failStory(Long storyId, Story.StoryStatus failedStatus) {

        // 1. 실패 상테 해당하는 enum 값 검증
        if (!failedStatus.isFailedStatus()) {
            throw new CustomException(ErrorStatus.STORY_INVALID_STATUS);
        }

        // 2. 실패 상태 업데이트
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        story.setStatus(failedStatus);
    }
}