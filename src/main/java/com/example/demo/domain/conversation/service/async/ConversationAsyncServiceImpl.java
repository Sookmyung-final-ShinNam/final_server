package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.ConversationMessage;
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
    public void completeStory(Long sessionId) {

        // 1. Story 조회 (상태 확인용 객체)
        Story story = storyRepo.findByStorySessions_Id(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        Long storyId = story.getId();

        if (story.getStatus() == Story.StoryStatus.READY_IMAGE || story.getStatus() == Story.StoryStatus.READY_VIDEO) {
            System.out.println("이미 이미지까지 생성된 스토리: storyId=" + storyId); // 생성 완료된 스토리는 생략
            return;
        }

        // 2. LLM 호출 및 Story/Character/StoryPage 업데이트 (스토리 정제)
        if (story.getStatus() == Story.StoryStatus.MAKING) {

            // 3. 이전 대화 조회
            String context = conversationQueryService.findSessionContextById(sessionId);
            conversationCompleteCommandService.completeStoryFromLlm(storyId, context);

            // 4. completeStoryFromLlm 커밋 이후 Story 최신 상태 재조회
            story = storyRepo.findById(storyId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));
        }

        // 5. 캐릭터 및 StoryPage 이미지 생성 (이미지 생성)
        if (story.getStatus() == Story.StoryStatus.COMPLETED) {
            conversationCompleteCommandService.generateStoryMedia(storyId, "image");
        }

        System.out.println("비동기 작업 완료: storyId=" + storyId + ", status=" + story.getStatus() + "스토리 정제 및 페이지 이미지 생성 이벤트 발행 완료");
    }

    @Async
    @Override
    @Transactional
    public void generateStoryVideo(Long storyId) {
        conversationCompleteCommandService.generateStoryMedia(storyId, "video");
        System.out.println("비동기 작업 완료: storyId=" + storyId + " 동영상 생성 완료");
    }

}