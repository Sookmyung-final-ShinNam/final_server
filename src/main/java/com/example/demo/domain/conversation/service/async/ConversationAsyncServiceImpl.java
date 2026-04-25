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
    private final StoryRepository storyRepo;

    private final ConversationCompleteCommandService conversationCompleteCommandService;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationSessionRepository sessionRepository;
    private final SessionStepRepository stepRepository;
    private final LlmClient llmClient;

    /**
     * 다음 Step 초기화 (트랜잭션 영역)
     *
     * 역할:
     * - session.currentStep → 다음 step으로 변경
     * - SessionStep 상태 IN_PROGRESS 설정
     * - prevContext 세팅
     * - 이후 이벤트 발행해서 nextStory + llmQuestion 생성
     *
     */
    @Transactional
    public void startNextStep(Long sessionId) {

        // 1. 세션 조회
        ConversationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 다음 step 계산
        ConversationSession.ConversationStep nextStep =
                getNextStep(session.getCurrentStep());

        if (nextStep == null) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 3. 해당 step 조회
        SessionStep step = stepRepository
                .findBySessionIdAndStepType(sessionId, nextStep)
                .orElseThrow(() -> new CustomException(ErrorStatus.STEP_NOT_FOUND));

        // 4. 중복 실행 방지
        if (step.getStatus() == SessionStep.Status.IN_PROGRESS) {
            throw new CustomException(ErrorStatus.STEP_ALREADY_IN_PROGRESS);
        }

        // 5. 상태 변경
        session.setCurrentStep(nextStep);
        step.setStatus(SessionStep.Status.IN_PROGRESS);

        // 6. 이전 스토리 컨텍스트 저장
        step.setPrevContext(session.getFullStory());

        // 7. llm 호출
        generateStepContent(step); // ✔ 직접 호출
    }

    /**
     * LLM 호출 + 결과 저장 (트랜잭션)
     *
     * 역할:
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
                throw new CustomException(ErrorStatus.LLM_RESPONSE_INVALID);
            }

            step.setNextStory(nextStory);
            step.setLlmQuestion(question);
            step.setStatus(SessionStep.Status.COMPLETED);

        } catch (Exception e) {
            throw new CustomException(ErrorStatus.LLM_CALL_FAILED);
        }
    }

    /**
     * 다음 Step 계산
     *
     * 흐름:
     * START → 기 → 승 → 전 → 결 → 종료(null)
     */
    private ConversationSession.ConversationStep getNextStep(
            ConversationSession.ConversationStep current
    ) {
        return switch (current) {
            case START -> ConversationSession.ConversationStep.기;
            case 기 -> ConversationSession.ConversationStep.승;
            case 승 -> ConversationSession.ConversationStep.전;
            case 전 -> ConversationSession.ConversationStep.결;
            case 결 -> ConversationSession.ConversationStep.END;
            case END -> null; // 마지막 단계
        };
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