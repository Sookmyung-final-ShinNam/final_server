package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationFeedback;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.ConversationStartedEvent;
import com.example.demo.domain.conversation.repository.ConversationFeedbackRepository;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationFeedbackCommandServiceImpl implements ConversationFeedbackCommandService {

    private final ConversationFeedbackRepository feedbackRepo;
    private final ConversationMessageRepository messageRepo;

    private final ConversationConverter converter;
    private final ConversationQueryService conversationQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final LlmClient llmClient;

    @Override
    @Transactional
    public ConversationResponseDto.FeedbackResponseDto handleFeedback(Long messageId, String userAnswer) {

        // 1. 메시지 조회
        ConversationMessage message = messageRepo.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 이미 GOOD 처리된 메시지라면 더 이상 피드백 불가
        if (message.getLlmAnswer() != null) {
            throw new CustomException(ErrorStatus.FEEDBACK_ALREADY_COMPLETED);
        }

        // 2. 세션 context 조회
        String context = conversationQueryService.findSessionContextById(message.getSession().getId());

        // 3. LLM 호출 준비
        int feedbackCount = (message.getFeedbacks() == null ? 0 : message.getFeedbacks().size()) + 1;
        String promptFileName = getPromptFile(feedbackCount);
        String variable = llmClient.jsonEscape("이전 상황: " + context + "\nllm 질문: " + message.getLlmQuestion() + "\n사용자 답변: " + userAnswer);

        // 4. LLM 호출
        String prompt = llmClient.buildPrompt(promptFileName, variable);
        String llmResponse = llmClient.callChatGpt(prompt);

        // 5. LLM 응답 파싱
        String feedbackResult = llmClient.extractFieldValue(llmResponse, "feedbackResult");   // "GOOD" or "NEEDS_CORRECTION"
        String feedbackText = llmClient.extractFieldValue(llmResponse, "feedbackText");       // 피드백 설명 텍스트

        // 6. Feedback 엔티티 생성 및 저장
        ConversationFeedback feedback = converter.toConversationFeedback(userAnswer, feedbackResult, feedbackText, feedbackCount, message);
        feedbackRepo.save(feedback);

        // 7. Good 피드백인 경우
        if (feedback.isCorrect()) {
            ConversationSession.ConversationStep currentStep = message.getSession().getCurrentStep();

            // message 엔티티 업데이트
            message.getFeedbacks().add(feedback);
            message.setLlmAnswer(feedbackText);
            messageRepo.save(message);

            // 별도 트랜잭션에서 다음 단계 준비
            processNextStepAsync(message.getSession().getId(), message.getSession().getCurrentStep());
        }

        // 8. 응답 DTO 생성
        return ConversationResponseDto.FeedbackResponseDto.builder()
                .feedbackResult(feedbackResult)
                .feedbackText(feedbackText)
                .currentStep(message.getSession().getCurrentStep())
                .tryNum(feedbackCount)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNextStepAsync(Long sessionId, ConversationSession.ConversationStep currentStep) {
        if (currentStep != ConversationSession.ConversationStep.END) {

            // 다음 단계 계산
            ConversationSession.ConversationStep[] steps = ConversationSession.ConversationStep.values();
            int nextStepIndex = currentStep.ordinal() + 1;
            ConversationSession.ConversationStep nextStep = steps[nextStepIndex];

            // === EVENT: 비동기로 다음 단계 사전 생성 작업 실행 ===
            eventPublisher.publishEvent(
                    new ConversationStartedEvent(sessionId, nextStep)
            );

        }
    }

    private String getPromptFile(int tryCount) {
        switch (tryCount) {
            case 1:
            case 2:
                return "feedback_try.json";
            case 3:
                return "feedback_try_success.json";
            default:
                throw new CustomException(ErrorStatus.COMMON_BAD_REQUEST);
        }

    }

}