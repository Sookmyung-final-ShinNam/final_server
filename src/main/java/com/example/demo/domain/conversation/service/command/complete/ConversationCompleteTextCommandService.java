package com.example.demo.domain.conversation.service.command.complete;

import com.example.demo.domain.story.entity.Story;

public interface ConversationCompleteTextCommandService {

    /**
     * 동화 정보 업데이트(상태, 제목, 3줄 요약)
     * 캐릭터 정보 업데이트(성격)
     * 동화 페이지 생성(정리된 내용)
     *
     * 다음 단계(이미지 생성) 진행을 위해 TEXT_COMPLETED 상태 반환
     */
    void completeStoryFromLlm(Long storyId, Long sessionId);
}
