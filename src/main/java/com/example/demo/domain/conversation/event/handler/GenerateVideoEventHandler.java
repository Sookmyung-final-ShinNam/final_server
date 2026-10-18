package com.example.demo.domain.conversation.event.handler;

import com.example.demo.domain.conversation.event.GenerateVideoEvent;
import com.example.demo.domain.conversation.service.command.complete.ConversationCompleteMediaCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateVideoEventHandler {

    private final ConversationCompleteMediaCommandService conversationCompleteMediaCommandService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(GenerateVideoEvent event) {

        long storyId = event.getStoryId();
        log.info("[Video Event] 동영상 생성 이벤트 발행 성공 storyId={}", storyId);

        conversationCompleteMediaCommandService.generateStoryMedia(storyId, "video");
    }
}
