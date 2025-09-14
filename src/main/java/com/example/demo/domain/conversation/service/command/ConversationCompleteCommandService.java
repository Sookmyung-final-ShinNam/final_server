package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.story.entity.Story;

public interface ConversationCompleteCommandService {

    /**
     * 동화 정보 업데이트(제목, 3줄 요약)
     * 캐릭터 정보 업데이트(성격)
     * 동화 페이지 생성(정리된 내용)
     */
    void completeStoryFromLlm(Story story, String context);

    /**
     * 캐릭터 정보 업데이트(기본 이미지) -> 이미지에서만
     * 동화 페이지 업데이트(각 페이지별 이미지 or 비디오)
     */
    void generateStoryMedia(Long storyId, String imageType);

}