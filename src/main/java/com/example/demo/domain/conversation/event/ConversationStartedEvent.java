package com.example.demo.domain.conversation.event;

import com.example.demo.domain.conversation.entity.ConversationSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConversationStartedEvent {
    private final Long sessionId;
    private final ConversationSession.ConversationStep nextStep;
}