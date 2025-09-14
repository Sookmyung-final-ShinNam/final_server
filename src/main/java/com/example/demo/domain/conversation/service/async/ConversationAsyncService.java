package com.example.demo.domain.conversation.service.async;

import com.example.demo.domain.conversation.entity.ConversationSession;

public interface ConversationAsyncService {

    /**
     * 비동기로 다음 단계 사전 생성 작업 수행
     * @param sessionId 대화 세션 ID
     * @param nextStep 다음 단계 (STEP_01, STEP_02, STEP_03, END)
     */
    void prepareNextStep(Long sessionId, ConversationSession.ConversationStep nextStep);

    /**
     * 스토리 완료 처리
     * - Story 상태 MAKING 으로 변경
     * - Story 업데이트 (제목, 3줄 요약)
     * - StoryPage 생성 후 저장
     * - StoryCharacter 업데이트 (성격, 기본 이미지)
     * - StoryPage 업데이트 (각 장별 이미지)
     * - Story, StoryCharacter 상태 COMPLETED 으로 변경
     */
    void storyComplete(Long sessionId);

}