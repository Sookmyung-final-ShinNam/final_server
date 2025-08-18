package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.model.LlmClient;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationAsyncServiceImpl implements ConversationAsyncService {

    private final ConversationMessageRepository messageRepo;
    private final ConversationSessionRepository sessionRepo;

    private final ConversationQueryService conversationQueryService;
    private final ConversationConverter converter;
    private final LlmClient llmClient;

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

}