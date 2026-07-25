package com.example.demo.domain.story.service.command;

public interface StoryCommandService {

    /**
     * 스토리 비디오 상태를 MAKING으로 변경 및 개별 페이지 동영상 생성 진행
     * @param storyId 스토리 ID
     */
    void generateVideo(Long storyId);
}