package com.example.demo.domain.story.service.command;

public interface StoryCommandService {

    /**
     * 스토리 비디오 상태를 MAKING으로 변경
     * @param storyId 스토리 ID
     */
    void markStoryVideoAsMaking(Long storyId);
}