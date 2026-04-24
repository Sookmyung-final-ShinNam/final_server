package com.example.demo.domain.conversation.service.query;

import com.example.demo.domain.conversation.entity.ConversationSession;

public interface ConversationQueryService {

    /**
     * 세션 ID로 대화 세션을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 대화 세션
     */
    ConversationSession findSessionById(Long sessionId);

}