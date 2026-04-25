package com.example.demo.domain.conversation.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationQueryServiceImpl implements ConversationQueryService {

    private final ConversationSessionRepository sessionRepo;
    private final SessionStepRepository stepRepository;

    /**
     * 세션 ID로 대화 세션을 조회합니다.
     * @param sessionId 대화 세션 ID
     * @return 대화 세션
     * */
    @Override
    @Transactional
    public Object getNextStepMessage(Long sessionId, ConversationSession.ConversationStep stepType) {

        // 1. 세션 조회
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        ConversationSession.ConversationStep current = session.getCurrentStep();

        // 2. 현재 단계만 허용 (StoryStep → ConversationStep 변환해서 비교)
        if (current != stepType) {
            throw new CustomException(ErrorStatus.STEP_INVALID_ACCESS);
        }

        // 3. step 조회
        SessionStep step = stepRepository
                .findBySessionIdAndStepType(sessionId, stepType)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));

        // 4. 아직 생성 안됨 → PENDING
        if (step.getNextStory() == null || step.getLlmQuestion() == null) {
            return ConversationResponseDto.StepPendingResponseDto.builder()
                    .status("PENDING")
                    .build();
        }

        // 5. 정상 응답
        return ConversationResponseDto.StepMessageResponseDto.builder()
                .messageId(step.getId())
                .nextStory(step.getNextStory())
                .llmQuestion(step.getLlmQuestion())
                .build();
    }

}