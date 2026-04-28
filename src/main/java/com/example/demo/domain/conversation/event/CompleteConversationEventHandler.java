package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.command.complete.ConversationCompleteOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteConversationEventHandler {

    private final ConversationCompleteOrchestrator conversationCompleteOrchestrator;

    /**
     * 스토리 생성 이벤트 핸들러
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompleteConversationEvent event) {
        conversationCompleteOrchestrator.orchestrateCompletion(event.getStoryId(), event.getSessionId());
    }
}