package com.example.demo.domain.conversation.service.command;

public interface ConversationCompleteCommandService {

    /**
     * 대화 완료(COMPLETED) 확인 후 스토리 업데이트 (상태-MAKING)
     * 스토리 생성 이벤트 발행
     */
    void completeConversation(Long sessionId);

    /**
     * 동화 정보 업데이트(제목, 3줄 요약)
     * 캐릭터 정보 업데이트(성격)
     * 동화 페이지 생성(정리된 내용)
     */
    void completeStoryFromLlm(Long storyId, String context);

    /**
     * 캐릭터 정보 업데이트(기본 이미지) -> 이미지에서만
     * 동화 페이지 업데이트(각 페이지별 이미지 or 비디오)
     */
    void generateStoryMedia(Long storyId, String imageType);

    /**
     * 개별 페이지 이미지 생성 및 페이지 상태 업데이트
     */
    void generateStoryImage(Long storyId, Long pageId, String basePrompt, Long Seed);

    /**
     * 생성 완료된 페이지 이미지 개수 확인 및 스토리 상태 업데이트
     */
    void aggregateStoryPage(Long storyId);
}