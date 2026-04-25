package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.*;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import com.example.demo.domain.conversation.repository.StepAttemptRepository;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConversationFeedbackCommandServiceImpl implements ConversationFeedbackCommandService {

    private final StepAttemptRepository stepAttemptRepository;
    private final SessionStepRepository stepRepository;
    private final LlmClient llmClient;

    @Override
    @Transactional
    public ConversationResponseDto.FeedbackResponseDto handleFeedback(Long messageId, String userAnswer) {

        // 1. Step 조회
        SessionStep step = getStep(messageId);
        System.out.println("[1] STEP 조회 완료: " + step.getId());

        // 2. attemptNo
        int attemptNo = getAttemptNo(step);
        System.out.println("[2] attemptNo = " + attemptNo);

        // 3. StepAttempt 생성
        StepAttempt attempt = createAttempt(step, attemptNo, userAnswer);
        System.out.println("[3] StepAttempt 생성 완료");

        // 4. LLM 입력 생성
        String userContent = buildUserContent(step, userAnswer);
        System.out.println("[4] userContent\n" + userContent);

        // 5. 프롬프트 + LLM 호출
        String response = callLlm(step, attemptNo, userContent);
        System.out.println("[5] LLM RESPONSE\n" + response);

        // 6. feedback 파싱
        String llmFeedback = llmClient.extractFieldValue(response, "llmFeedback");
        attempt.setLlmFeedback(llmFeedback);
        System.out.println("[6] llmFeedback = " + llmFeedback);

        // 7. slots 파싱 및 반영
        applySlotUpdates(step, response);
        System.out.println("[7] SLOT 업데이트 완료");

        // 8. 슬롯 완료 여부
        boolean allFilled = isAllSlotsFilled(step);
        System.out.println("[8] allFilled = " + allFilled);

        // 9. isCorrect
        boolean isCorrect = allFilled;
        attempt.setIsCorrect(isCorrect);
        System.out.println("[9] isCorrect = " + isCorrect);

        // 10. 완료 처리
        if (allFilled) {
            completeStep(step, llmFeedback);
            System.out.println("[10] STEP COMPLETED");
        }

        // 11. 저장
        stepAttemptRepository.save(attempt);
        System.out.println("[11] attempt 저장 완료");

        // 12. response
        return ConversationResponseDto.FeedbackResponseDto.builder()
                .feedbackResult(isCorrect ? "GOOD" : "NEEDS_CORRECTION")
                .feedbackText(llmFeedback)
                .currentStep(step.getStepType())
                .tryNum(attemptNo)
                .build();
    }

    // 1. STEP 조회
    private SessionStep getStep(Long messageId) {
        return stepRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));
    }

    // 2. attemptNo
    private int getAttemptNo(SessionStep step) {
        return step.getAttempts().size() + 1;
    }

    // 3. attempt 생성
    private StepAttempt createAttempt(SessionStep step, int attemptNo, String userAnswer) {
        return StepAttempt.builder()
                .step(step)
                .attemptNo(attemptNo)
                .userAnswer(userAnswer)
                .build();
    }

    // 4. LLM 입력
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

    // 5. LLM 호출
    private String callLlm(SessionStep step, int attemptNo, String userContent) {

        String promptFile = (attemptNo == 3)
                ? "story_feedback_v3.json"
                : "story_feedback_v12.json";

        String prompt = llmClient.buildPrompt(promptFile, llmClient.jsonEscape(userContent));

        return llmClient.callChatGpt(prompt);
    }

    // 7. SLOT 반영
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

    // 8. 완료 여부
    private boolean isAllSlotsFilled(SessionStep step) {
        return step.getSlots()
                .stream()
                .allMatch(s -> Boolean.TRUE.equals(s.getIsFilled()));
    }

    // 10. 완료 처리
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
}