package com.example.demo.domain.conversation.service.command.feedback;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.*;
import com.example.demo.domain.conversation.event.StartConversationEvent;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import com.example.demo.domain.conversation.repository.StepAttemptRepository;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConversationFeedbackCommandServiceImpl implements ConversationFeedbackCommandService {

    private final StepAttemptRepository stepAttemptRepository;
    private final SessionStepRepository stepRepository;
    private final LlmClient llmClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConversationResponseDto.FeedbackResponseDto handleFeedback(Long messageId, String userAnswer) {

        // 1. Step 조회
        SessionStep step = getStep(messageId);
        int attemptNo = getAttemptNo(step);

        // 3회 초과 방지
        if (attemptNo > 3) {
            throw new CustomException(ErrorStatus.FEEDBACK_ALREADY_COMPLETED);
        }

        // 2. StepAttempt 생성
        StepAttempt attempt = createAttempt(step, attemptNo, userAnswer);

        // 3. LLM 입력 생성
        String userContent = buildUserContent(step, userAnswer);

        // 4. 프롬프트 + LLM 호출
        String response = callLlm(step, attemptNo, userContent);

        // 5. feedback 파싱
        String llmFeedback = llmClient.extractFieldValue(response, "llmFeedback");
        attempt.setLlmFeedback(llmFeedback);

        // 6. slots 파싱 및 반영
        applySlotUpdates(step, response);

        // 7. 슬롯 완료 여부
        boolean allFilled = isAllSlotsFilled(step);
        boolean isCorrect = allFilled;
        attempt.setIsCorrect(isCorrect);

        // 8. 완료 처리
        if (isCorrect) {
            completeStep(step, llmFeedback);

            // 기승전 단계 → 다음 세션 미리 생성
            if (shouldStartNextStep(step)) {

                Long sessionId = step.getSession().getId();

                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCommit() {
                                eventPublisher.publishEvent(
                                        new StartConversationEvent(sessionId)
                                );
                            }
                        }
                );

            } else {
                // 결 단계 → 세션 종료 처리
                ConversationSession session = step.getSession();
                session.setCurrentStep(ConversationSession.ConversationStep.END);
                session.setState(ConversationSession.SessionState.COMPLETED);
            }
        }

        // 9. 저장
        stepAttemptRepository.save(attempt);

        return ConversationResponseDto.FeedbackResponseDto.builder()
                .feedbackResult(isCorrect ? "GOOD" : "NEEDS_CORRECTION")
                .feedbackText(llmFeedback)
                .currentStep(step.getStepType())
                .tryNum(attemptNo)
                .build();
    }

    // STEP 조회
    private SessionStep getStep(Long messageId) {
        return stepRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));
    }

    private int getAttemptNo(SessionStep step) {
        return step.getAttempts().size() + 1;
    }

    // attempt 생성
    private StepAttempt createAttempt(SessionStep step, int attemptNo, String userAnswer) {
        return StepAttempt.builder()
                .step(step)
                .attemptNo(attemptNo)
                .userAnswer(userAnswer)
                .build();
    }

    // LLM 입력
    private String buildUserContent(SessionStep step, String userAnswer) {

        String slotContext = step.getSlots().stream()
                .map(s -> s.getSlotDefinition().getSlotName()
                        + ": value=" + (s.getValue() != null ? s.getValue() : "null")
                        + ", source=" + (s.getSource() != null ? s.getSource() : "null"))
                .reduce("", (a, b) -> a + "\n" + b);

        return """
                prevContext:
                %s

                nextStory:
                %s

                llmQuestion:
                %s

                userAnswer:
                %s

                [SlotState]
                %s
                """.formatted(
                step.getPrevContext(),
                step.getNextStory(),
                step.getLlmQuestion(),
                userAnswer,
                slotContext
        );
    }

    // LLM 호출
    private String callLlm(SessionStep step, int attemptNo, String userContent) {

        String promptFile = (attemptNo == 3)
                ? "story_feedback_v3.json"
                : "story_feedback_v12.json";

        String prompt = llmClient.buildPrompt(promptFile, llmClient.jsonEscape(userContent));

        return llmClient.callChatGpt(prompt);
    }

    // SLOT 반영
    private void applySlotUpdates(SessionStep step, String response) {

        Map<String, Map<String, String>> slots =
                llmClient.extractFieldMap(response, "slots");

        step.getSlots().forEach(slot -> {

            String key = slot.getSlotDefinition().getSlotName();
            Map<String, String> slotResult = slots.get(key);

            if (slotResult == null) return;

            String value = slotResult.get("value");
            String source = slotResult.get("source");

            if (value != null && !slot.getIsFilled()) {
                slot.setValue(value);
                slot.setIsFilled(true);
                slot.setSource(source != null ? source : "USER");
            }

            System.out.println("[SLOT] " + key +
                    " value=" + value +
                    " source=" + source);
        });
    }

    // 완료 여부
    private boolean isAllSlotsFilled(SessionStep step) {
        return step.getSlots()
                .stream()
                .allMatch(s -> Boolean.TRUE.equals(s.getIsFilled()));
    }

    // 완료 처리
    private void completeStep(SessionStep step, String llmFeedback) {

        step.setFinalAnswer(llmFeedback);

        String updatedStory =
                step.getPrevContext() + "\n" +
                        step.getNextStory() + "\n" +
                        step.getLlmQuestion() + "\n" +
                        step.getFinalAnswer();

        step.getSession().setFullStory(updatedStory);

        step.setStatus(SessionStep.Status.COMPLETED);
    }

    private boolean shouldStartNextStep(SessionStep step) {

        ConversationSession.ConversationStep type = step.getStepType();

        return type == ConversationSession.ConversationStep.기
                || type == ConversationSession.ConversationStep.승
                || type == ConversationSession.ConversationStep.전;
    }
}