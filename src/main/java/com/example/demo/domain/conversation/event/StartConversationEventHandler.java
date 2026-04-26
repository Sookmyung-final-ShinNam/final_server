package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.command.ConversationNextStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartConversationEventHandler {

    private final ConversationNextStoryService conversationNextStoryService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StartConversationEvent event) {

        log.info("[START EVENT] sessionId={}", event.getSessionId());

        conversationNextStoryService.startNextStep(event.getSessionId());
    }
}