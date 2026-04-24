package com.example.demo.domain.conversation.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PageImageStartedEvent {
    private final Long storyId;
    private final Long pageId;
}
