package com.example.demo.domain.conversation.event;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationNextStoryEventHandler {

    private final SessionStepRepository stepRepo;
    private final LlmClient llmClient;

    @Transactional
    public void handle(ConversationNextStoryEvent event) {

        log.info("[EVENT HANDLE START] stepId={}", event.getStepId());

        SessionStep step = stepRepo.findWithSlotsById(event.getStepId())
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));

        // idempotency (중복 방지)
        if (step.getNextStory() != null) {
            log.info("[SKIP] already processed stepId={}", step.getId());
            return;
        }

        generateStepContent(step);

        log.info("[EVENT HANDLE END] stepId={}", step.getId());
    }

    private void generateStepContent(SessionStep step) {

        String slotList = step.getSlots().stream()
                .map(s -> s.getSlotDefinition().getSlotName())
                .collect(Collectors.joining(", "));

        try {
            String variable = llmClient.jsonEscape(
                    """
                    prevContext:
                    %s  
    
                    [SlotList]
                    %s
                    """.formatted(
                            step.getPrevContext(),
                            slotList
                    )
            );

            String promptJson = llmClient.buildPrompt(
                    "story_step_generate.json",
                    variable
            );

            String response = llmClient.callChatGpt(promptJson);

            String nextStory = llmClient.extractFieldValue(response, "nextStory");
            String question = llmClient.extractFieldValue(response, "llm_question");

            if (nextStory == null || question == null) {
                throw new CustomException(ErrorStatus.CHAT_GPT_API_RESPONSE_FAILED);
            }

            step.setNextStory(nextStory);
            step.setLlmQuestion(question);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.CHAT_GPT_API_CALL_FAILED);
        }
    }
}