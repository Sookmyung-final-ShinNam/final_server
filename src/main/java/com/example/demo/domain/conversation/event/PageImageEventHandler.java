package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.command.complete.ConversationCompleteMediaCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 페이지 생성 및 생성 완료 관련 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PageImageEventHandler {

    private final ConversationCompleteMediaCommandService conversationCompleteMediaCommandService;

    // 개별 페이지 이미지 생성 및 페이지 상태 업데이트
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePageImageStarted(StartPageImageEvent event) {
        log.info("[IMAGE2 START] storyId={} at={}", event.getStoryId(), System.currentTimeMillis());
        conversationCompleteMediaCommandService.generateStoryPageImage(event.getStoryId(), event.getPageId(), event.getBasePrompt(), event.getSeed());
    }

    // 생성 완료된 페이지 이미지 개수 확인 및 스토리 상태 업데이트 (동기)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePageImageCompleted(CompletePageImageEvent event) {
        conversationCompleteMediaCommandService.aggregateStoryPageImage(event.getStoryId());
    }
}
