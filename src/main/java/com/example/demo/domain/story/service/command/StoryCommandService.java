package com.example.demo.domain.story.service.command;

public interface StoryCommandService {

    /**
     * 스토리 비디오 상태를 MAKING으로 변경
     * @param storyId 스토리 ID
     */
    void markStoryVideoAsMaking(Long storyId);

    /**
     * 배치 대상 스토리의 retry_count를 +1하고 스토리 생성 이벤트 발행
     */
    void retryFailedStories(Long storyId, Long sessionId);
}