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

    /**
     * 세션 ID로 대화 세션의 맥락을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 대화 세션의 맥락 문자열
     */
    String findSessionContextById(Long sessionId);

    /**
     * 세션 ID로 최신 메시지를 조회하고, 생성된 다음 단계 메시지를 반환합니다.
     *
     * @param sessionId 대화 세션 ID
     * @param currentStep 현재 단계 (=메시지를 조회할 단계)
     * @return 다음 단계 메시지 응답 DTO
     */
    Object getNextStepMessage(Long sessionId, String currentStep);

}