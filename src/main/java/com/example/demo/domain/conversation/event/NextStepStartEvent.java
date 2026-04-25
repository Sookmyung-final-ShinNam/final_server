package com.example.demo.domain.conversation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NextStepStartEvent extends ApplicationEvent {

    private final Long sessionId;

    public NextStepStartEvent(Object source, Long sessionId) {
        super(source);
        this.sessionId = sessionId;
    }
}