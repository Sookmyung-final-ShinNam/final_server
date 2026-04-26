package com.example.demo.domain.conversation.service.query;

import com.example.demo.domain.conversation.entity.ConversationSession;

public interface ConversationQueryService {

    /**
     * 현재 세션의 진행 상태를 기준으로 다음 단계에서 사용자에게 반환할 메시지 조회
     *
     * @param sessionId 조회 대상
     * @param currentStep 현재 세션의 진행 단계 (기/승/전/결)
     * @return 다음 Step에서 사용자에게 전달할 메시지
     */
    Object getNextStepMessage(Long sessionId, ConversationSession.ConversationStep currentStep);

}