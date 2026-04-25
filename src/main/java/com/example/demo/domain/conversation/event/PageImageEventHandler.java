package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PageImageEventHandler {

    private final ConversationCompleteCommandService conversationCompleteCommandService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePageImageStarted(PageImageStartedEvent event) {
        conversationCompleteCommandService.generatePageImage(event.getStoryId(), event.getPageId(), event.getBasePrompt(), event.getSeed());
    }
}
