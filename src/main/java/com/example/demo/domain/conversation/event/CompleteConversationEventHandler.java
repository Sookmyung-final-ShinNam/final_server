package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.command.complete.ConversationCompleteOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 대화 세션 진행 후 스토리 생성 관련 핸들러
 *
 *  - CompleteConversationEvent: 비동기 로직
 *  - RetryStoryEvent: 동기 로직
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteConversationEventHandler {

    private final ConversationCompleteOrchestrator conversationCompleteOrchestrator;

    // 사용자 요청 - 스토리 생성 이벤트 핸들러 (비동기)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompleteConversationEvent event) {
        conversationCompleteOrchestrator.orchestrateCompletion(event.getStoryId(), event.getSessionId());
    }

    // 배치 작업 - 스토리 생성 이벤트 핸들러 (동기)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RetryStoryEvent event) {
        conversationCompleteOrchestrator.orchestrateCompletion(event.getStoryId(), event.getSessionId());
    }
}