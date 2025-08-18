package com.example.demo.domain.conversation.service.async;

import com.example.demo.domain.conversation.entity.ConversationSession;

public interface ConversationAsyncService {

    /**
     * 비동기로 다음 단계 사전 생성 작업 수행
     * @param sessionId 대화 세션 ID
     * @param nextStep 다음 단계 (STEP_01, STEP_02, STEP_03, END)
     */
    void prepareNextStep(Long sessionId, ConversationSession.ConversationStep nextStep);
}