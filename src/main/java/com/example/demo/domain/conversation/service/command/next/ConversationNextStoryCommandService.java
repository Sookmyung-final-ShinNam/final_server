package com.example.demo.domain.conversation.service.command.next;

public interface ConversationNextStoryCommandService {

    void startNextStep(Long sessionId);
}