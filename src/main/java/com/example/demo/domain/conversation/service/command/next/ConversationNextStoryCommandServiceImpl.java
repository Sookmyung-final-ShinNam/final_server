package com.example.demo.domain.conversation.service.command.next;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.event.ConversationNextStoryEvent;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationNextStoryCommandServiceImpl implements ConversationNextStoryCommandService {

    private final ConversationSessionRepository sessionRepo;
    private final SessionStepRepository stepRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void startNextStep(Long sessionId) {

        // 1. 세션 조회
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        ConversationSession.ConversationStep current = session.getCurrentStep();

        // 2. 현재 step 조회
        SessionStep currentStep = null;

        if (current != ConversationSession.ConversationStep.START) {
            currentStep = stepRepo
                    .findBySessionAndStepType(session, current)
                    .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));
        }

        // 3. 다음 step 계산 + 검증
        ConversationSession.ConversationStep nextStepType;

        switch (current) {
            case START -> {
                // 시작 단계 → 바로 기 단계로
                nextStepType = ConversationSession.ConversationStep.기;
            }
            case 기 -> {
                // 현재 step 완료 여부 검증
                validateCompleted(currentStep);
                nextStepType = ConversationSession.ConversationStep.승;
            }
            case 승 -> {
                validateCompleted(currentStep);
                nextStepType = ConversationSession.ConversationStep.전;
            }
            case 전 -> {
                validateCompleted(currentStep);
                nextStepType = ConversationSession.ConversationStep.결;
            }
            default -> throw new CustomException(ErrorStatus.STEP_NOT_FOUND);
        }

        // 4. 세션 상태 이동
        session.setCurrentStep(nextStepType);

        // 5. 다음 step 조회
        SessionStep nextStep = stepRepo
                .findBySessionAndStepType(session, nextStepType)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));

        // 6. 상태 변경
        nextStep.setStatus(SessionStep.Status.IN_PROGRESS);

        // 7. prevContext 세팅 (LLM 입력 데이터)
        nextStep.setPrevContext(session.getFullStory());

        // 8. llm 호출 - commit 이후 이벤트 발행
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(
                                new ConversationNextStoryEvent(nextStep.getId())
                        );
                    }
                }
        );

    }

    // step 완료 여부 검증
    private void validateCompleted(SessionStep step) {
        if (step == null || step.getStatus() != SessionStep.Status.COMPLETED) {
            throw new CustomException(ErrorStatus.STEP_NOT_COMPLETED);
        }
    }

}