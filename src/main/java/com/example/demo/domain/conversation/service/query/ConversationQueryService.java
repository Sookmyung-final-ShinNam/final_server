package com.example.demo.domain.conversation.service.query;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.user.entity.User;

import java.util.List;

public interface ConversationQueryService {

    /**
     * 현재 세션의 진행 상태를 기준으로 다음 단계에서 사용자에게 반환할 메시지 조회
     *
     * @param sessionId 조회 대상
     * @param currentStep 현재 세션의 진행 단계 (기/승/전/결)
     * @return 다음 Step에서 사용자에게 전달할 메시지
     */
    Object getNextStepMessage(Long sessionId, ConversationSession.ConversationStep currentStep);

    /**
     * 이어하기: 사용자가 끝내지 않은(진행 중) 대화 세션 목록 조회 (최신순)
     *
     * 일반 동화와 학급 과제 동화가 함께 조회되며, 학급 과제는 assignmentId/classroomId 값이 있음
     */
    List<ConversationResponseDto.InProgressSessionDto> getInProgressSessions(User user);

}