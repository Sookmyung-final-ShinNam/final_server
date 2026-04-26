package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.event.StoryCompletedEvent;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.repository.SessionStepRepository;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAsyncServiceImpl implements ConversationAsyncService {

    private final ConversationSessionRepository sessionRepo;
    private final SessionStepRepository stepRepo;
    private final StoryRepository storyRepo;

    private final ConversationCompleteCommandService conversationCompleteCommandService;
    private final ApplicationEventPublisher eventPublisher;
    private final LlmClient llmClient;

    @Async
    @Override
    @Transactional
    public void startNextStep(Long sessionId) {

        log.info("[STEP-START] sessionId={}", sessionId);

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        ConversationSession.ConversationStep current = session.getCurrentStep();
        log.info("[CURRENT STEP] sessionId={}, current={}", sessionId, current);

        // 1. 현재 step 엔티티 조회
        SessionStep currentStep = stepRepo
                .findBySessionAndStepType(session, current)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));

        // 2. 다음 step 계산 + 검증
        ConversationSession.ConversationStep nextStepType;

        switch (current) {
            case START -> {
                nextStepType = ConversationSession.ConversationStep.기;
            }
            case 기 -> {
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
        log.info("[NEXT STEP] sessionId={}, next={}", sessionId, nextStepType);

        // 3. 세션 step 이동
        session.setCurrentStep(nextStepType);

        // 4. 다음 step 조회
        SessionStep nextStep = stepRepo
                .findBySessionAndStepType(session, nextStepType)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));
        log.info("[STEP STATUS BEFORE] step={}, status={}", nextStepType, nextStep.getStatus());

        // 5. 상태 변경
        nextStep.setStatus(SessionStep.Status.IN_PROGRESS);

        // 6. prevContext 세팅
        nextStep.setPrevContext(session.getFullStory());
        log.info("[LLM CALL START] step={}", nextStepType);

        // 7. LLM 호출
        generateStepContent(nextStep);
    }

    private void validateCompleted(SessionStep step) {
        if (step.getStatus() != SessionStep.Status.COMPLETED) {
            throw new CustomException(ErrorStatus.STEP_NOT_COMPLETED);
        }
    }

    /**
     * LLM 호출 + 결과 저장
     * - nextStory 생성
     * - llmQuestion 생성
     */
    private void generateStepContent(SessionStep step) {

        String slotList = step.getSlots().stream()
                .map(s -> s.getSlotDefinition().getSlotName())
                .collect(Collectors.joining(", "));
        log.info("slotList = {}", slotList);

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




    @Async
    @Override
    @Transactional
    public void storyComplete(Long sessionId) {

        // 1. Story 조회
        Story story = storyRepo.findByStorySessions_Id(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 현재 단계가 END 인지 확인
        if (session.getCurrentStep() != ConversationSession.ConversationStep.END) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 4. 상태 변경 -> MAKING 에서는 이어하기 불가
        markStoryMaking(story); // 중간 트랜젝션 처리

        // 5. 이전 대화 조회
        String context = "";

        // 6. LLM 호출 및 Story/Character/StoryPage 업데이트
        conversationCompleteCommandService.completeStoryFromLlm(story, context);

        // 7. 캐릭터 및 StoryPage 이미지 생성
        conversationCompleteCommandService.generateStoryMedia(story.getId(), "image");

        // 8. 최종 상태 업데이트
        story.setStatus(Story.StoryStatus.COMPLETED);
        story.getCharacter().setStatus(StoryCharacter.CharacterStatus.COMPLETED);

        // 9. 이벤트 발행 (트랜잭션 커밋 후 처리)
        eventPublisher.publishEvent(new StoryCompletedEvent(this, story.getId(), story.getUser().getId()));

        System.out.println("비동기 작업 완료: storyStatus=" + story.getStatus());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStoryMaking(Story story) {
        story.setStatus(Story.StoryStatus.MAKING);
    }

    @Async
    @Override
    @Transactional
    public void generateStoryVideo(Long storyId) {
        conversationCompleteCommandService.generateStoryMedia(storyId, "video");
        System.out.println("비동기 작업 완료: storyId=" + storyId + " 동영상 생성 완료");
    }

}