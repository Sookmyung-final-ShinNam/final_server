package com.example.demo.domain.conversation.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationSessionRepository sessionRepo;

    private final ConversationConverter converter;

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

    /**
     * 세션 ID로 대화 세션의 맥락을 조회합니다.
     *
     * @param sessionId 대화 세션 ID
     * @return 대화 세션의 맥락 문자열
     */
    @Override
    @Transactional
    public String findSessionContextById(Long sessionId) {

        // 세션 조회
        ConversationSession session = findSessionById(sessionId);

        // 메시지 목록 가져오기
        List<ConversationMessage> messages = session.getMessages();

        // 메시지 기반 context 생성
        return buildContextFromMessages(messages);
    }

    private String buildContextFromMessages(List<ConversationMessage> messages) {
        // next -> llmQuestion -> llmAnswer 형태로 맥락 생성
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage msg : messages) {
            sb.append(msg.getNextStory()).append("\n");
            sb.append(msg.getLlmQuestion()).append("\n");
            sb.append(msg.getLlmAnswer()).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public Object getNextStepMessage(Long sessionId, String currentStepStr) {

        // 1. 세션 조회
        ConversationSession session = findSessionById(sessionId);

        // 2. currentStepStr을 enum으로 변환
        ConversationSession.ConversationStep clientCurrentStep;
        try {
            clientCurrentStep = ConversationSession.ConversationStep.valueOf(currentStepStr);
        } catch (IllegalArgumentException e) {
            return Map.of("status", "INVALID_STEP");
        }

        // 3. 서버의 실제 현재 단계
        ConversationSession.ConversationStep serverCurrentStep = session.getCurrentStep();

        // 4. 클라이언트가 인지하는 단계와 서버가 관리하는 현재 단계 비교
        if (clientCurrentStep != serverCurrentStep) {
            // 클라이언트가 아직 최신 단계를 받지 못한 상태 (다르면 PENDING)
            return Map.of("status", "PENDING");
        }

        // 5. 최신 메시지 조회
        List<ConversationMessage> messages = session.getMessages();

        if (messages.isEmpty()) {
            return Map.of("status", "PENDING");
        }

        ConversationMessage latestMessage = messages.get(messages.size() - 1);

        // 6. DTO 변환 후 반환
        return converter.toNextStepResponseDto(latestMessage);
    }

}