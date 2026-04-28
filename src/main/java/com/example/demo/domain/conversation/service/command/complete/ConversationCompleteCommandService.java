package com.example.demo.domain.conversation.service.command.complete;

import com.example.demo.domain.story.entity.Story;

public interface ConversationCompleteCommandService {

    /**
     * 대화 완료(COMPLETED) 확인 후,
     * 스토리 상태 업데이트 및 스토리 생성 이벤트 발행
     */
    void completeStory(Long sessionId);

    /**
     * 스토리 생성 진행 실패 -> 스토리 상태 업데이트
     */
    void failStory(Long storyId, Story.StoryStatus failedStatus);
}