package com.example.demo.domain.conversation.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StartPageImageEvent {
    private final Long storyId;
    private final Long pageId;
    private final String basePrompt;
    private final Long seed;
}
