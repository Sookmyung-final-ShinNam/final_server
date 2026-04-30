package com.example.demo.domain.conversation.service.command.complete;

public interface ConversationCompleteMediaCommandService {

    /**
     * 캐릭터 정보 업데이트(기본 이미지) -> 이미지에서만
     * 동화 페이지 업데이트(각 페이지별 이미지 or 비디오)
     */
    void generateStoryMedia(Long storyId, String imageType);

    /**
     * 이벤트 처리 로직 - 개별 페이지 이미지 생성 및 페이지 상태 업데이트
     */
    void generateStoryPageImage(Long storyId, Long pageId, String basePrompt, Long Seed);

    /**
     * 이벤트 처리 로직 - 생성 완료된 페이지 이미지 개수 확인 및 스토리 상태 업데이트
     */
    void aggregateStoryPageImage(Long storyId);
}