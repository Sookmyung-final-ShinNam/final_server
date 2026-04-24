package com.example.demo.domain.conversation.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import com.example.demo.domain.conversation.service.async.ConversationAsyncService;

@Component
@RequiredArgsConstructor
public class ConversationStartEventHandler {

    private final ConversationAsyncService asyncService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConversationStarted(ConversationStartedEvent event) {
        asyncService.prepareNextStep(event.getSessionId(), event.getNextStep());
    }
}