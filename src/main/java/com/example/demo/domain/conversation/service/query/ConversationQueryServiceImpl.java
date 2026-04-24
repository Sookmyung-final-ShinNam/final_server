package com.example.demo.domain.conversation.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationSessionRepository sessionRepo;

    /**
     * 세션 ID로 대화 세션을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 대화 세션
     */
    @Override
    @Transactional
    public ConversationSession findSessionById(Long sessionId) {

        // 세션 조회
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));
    }

}