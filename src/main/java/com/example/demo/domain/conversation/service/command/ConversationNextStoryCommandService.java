package com.example.demo.domain.conversation.service.command;

public interface ConversationNextStoryCommandService {

    void startNextStep(Long sessionId);
}