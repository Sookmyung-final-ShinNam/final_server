package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.async.ConversationAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ConversationEventListener {

    private final ConversationAsyncService asyncService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNextStep(NextStepStartEvent event) {
        asyncService.startNextStep(event.getSessionId()); // 기승전결에서만 실행
    }
}