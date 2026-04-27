package com.example.demo.domain.conversation.service.command;

public interface ConversationNextStoryService {

    void startNextStep(Long sessionId);
}