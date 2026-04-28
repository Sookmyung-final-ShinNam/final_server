package com.example.demo.domain.conversation.service.async;

public interface ConversationAsyncService {

    /**
     * 동영상 동화 생성
     * - StoryCharacter 기본 이미지로 동영상 생성
     */
    void generateStoryVideo(Long storyId);

}