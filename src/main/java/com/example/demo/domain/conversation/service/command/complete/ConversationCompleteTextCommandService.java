package com.example.demo.domain.conversation.service.command.complete;

public interface ConversationCompleteTextCommandService {

    /**
     * 동화 정보 업데이트(제목, 3줄 요약)
     * 캐릭터 정보 업데이트(성격)
     * 동화 페이지 생성(정리된 내용)
     */
    void completeStoryFromLlm(Long storyId, Long sessionId);
}
