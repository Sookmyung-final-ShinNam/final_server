package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.event.StoryCompletedEvent;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationAsyncServiceImpl implements ConversationAsyncService {

    private final ConversationMessageRepository messageRepo;
    private final ConversationSessionRepository sessionRepo;
    private final StoryRepository storyRepo;

    private final ConversationQueryService conversationQueryService;
    private final ConversationCompleteCommandService conversationCompleteCommandService;
    private final ConversationConverter converter;
    private final LlmClient llmClient;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @Override
    @Transactional
    public void prepareNextStep(Long sessionId, ConversationSession.ConversationStep nextStep) {

        // 1. 세션 객체와 context 문자열 조회
        ConversationSession session = conversationQueryService.findSessionById(sessionId);
        String context = conversationQueryService.findSessionContextById(sessionId);

        // 2. LLM 호출 준비
        String promptFileName = getPromptFile(nextStep);
        String variable = llmClient.jsonEscape("이전 상황: " + context);

        // 3. LLM 호출
        String prompt = llmClient.buildPrompt(promptFileName, variable);
        String llmResponse = llmClient.callChatGpt(prompt);

        // 4. LLM 응답 파싱
        String nextStory = llmClient.extractFieldValue(llmResponse, "nextStory");
        String llmQuestion = llmClient.extractFieldValue(llmResponse, "llmQuestion");

        // 5. 새로운 메시지 생성 및 저장
        ConversationMessage newMessage = converter.toConversationMessage(nextStory, llmQuestion, session);
        messageRepo.save(newMessage);

        // 6. 세션 업데이트 및 저장
        session.addMessage(newMessage);
        session.setCurrentStep(nextStep);
        sessionRepo.save(session);

        System.out.println("비동기 작업 완료: sessionId=" + sessionId + ", nextStep=" + nextStep);
    }

    private String getPromptFile(ConversationSession.ConversationStep nextStep) {
        switch (nextStep) {
            case STEP_01:
                return "story_next_step01.json";
            case STEP_02:
                return "story_next_step02.json";
            case STEP_03:
                return "story_next_step03.json";
            case END:
                return "story_next_end.json";
            default:
                throw new CustomException(ErrorStatus.COMMON_BAD_REQUEST);
        }
    }

    @Async
    @Override
    @Transactional
    public void completeStory(Long storyId, Long sessionId) {

        // 1. Story 및 Session 조회
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));


        // 2. 이전 대화 조회
        String context = "";
        if (!story.getStorySessions().isEmpty()) {
            context = conversationQueryService.findSessionContextById(session.getId());
        } else {
            throw new CustomException(ErrorStatus.STORY_NOT_FOUND);
        }

        // 3. LLM 호출 및 Story/Character/StoryPage 업데이트
        conversationCompleteCommandService.completeStoryFromLlm(story, context);
        story.setStatus(Story.StoryStatus.COMPLETED); // 텍스트 생성 완료

        // 4. 캐릭터 이미지 및 StoryPage 이미지 생성
        conversationCompleteCommandService.generateStoryMedia(story.getId(), "image");

        // 5. 최종 상태 업데이트
        //story.setStatus(Story.StoryStatus.READY_IMAGE); // 페이지 이미지 생성 완료
        //story.getCharacter().setStatus(StoryCharacter.CharacterStatus.COMPLETED); // 캐릭터 이미지 생성 완료

        // 9. 이벤트 발행 (트랜잭션 커밋 후 처리)
        //eventPublisher.publishEvent(new StoryCompletedEvent(this, story.getId(), story.getUser().getId()));

        System.out.println("비동기 작업 완료: storyStatus=" + story.getStatus());
    }

    @Async
    @Override
    @Transactional
    public void generateStoryVideo(Long storyId) {
        conversationCompleteCommandService.generateStoryMedia(storyId, "video");
        System.out.println("비동기 작업 완료: storyId=" + storyId + " 동영상 생성 완료");
    }

}