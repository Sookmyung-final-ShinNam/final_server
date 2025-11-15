package com.example.demo.domain.conversation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StoryCompletedEvent extends ApplicationEvent {

    private final Long storyId;
    private final Long userId;

    public StoryCompletedEvent(Object source, Long storyId, Long userId) {
        super(source);
        this.storyId = storyId;
        this.userId = userId;
    }

}