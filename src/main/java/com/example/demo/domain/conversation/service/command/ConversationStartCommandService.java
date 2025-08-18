package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.user.entity.User;

/**
 * 대화 시작부 Command 서비스 인터페이스
 */
public interface ConversationStartCommandService {

    /**
     * 대화 세션 시작 요청을 처리하고,
     * Story, Character, Session 생성 및 LLM 호출을 통해 첫 문장 생성 후 결과 반환
     *
     * @param user    대화 요청을 보낸 사용자
     * @param request 대화 시작 요청 DTO
     * @return 시작 응답 DTO (sessionId, nextStory)
     */
    ConversationResponseDto.ConversationStartResponseDto startConversation(ConversationRequestDto.ConversationStartRequestDto request, User user);

}