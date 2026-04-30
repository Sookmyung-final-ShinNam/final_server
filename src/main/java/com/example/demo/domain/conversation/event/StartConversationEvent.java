package com.example.demo.domain.conversation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartConversationEvent {
    private final Long sessionId;
}