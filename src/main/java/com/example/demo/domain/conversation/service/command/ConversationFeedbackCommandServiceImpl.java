package com.example.demo.domain.conversation.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationFeedback;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationFeedbackRepository;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.service.async.ConversationAsyncService;
import com.example.demo.domain.conversation.service.model.LlmClient;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationFeedbackCommandServiceImpl implements ConversationFeedbackCommandService {

    private final ConversationFeedbackRepository feedbackRepo;
    private final ConversationMessageRepository messageRepo;

    private final ConversationConverter converter;
    private final ConversationQueryService conversationQueryService;
    private final ConversationAsyncService asyncService;
    private final LlmClient llmClient;

    @Override
    @Transactional
    public ConversationResponseDto.FeedbackResponseDto handleFeedback(
            ConversationRequestDto.FeedbackRequestDto request
    ) {

        // 1. 메시지 조회
        ConversationMessage message = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 세션 context 조회
        String context = conversationQueryService.findSessionContextById(message.getSession().getId());

        // 3. LLM 호출 준비
        int feedbackCount = (message.getFeedbacks() == null ? 0 : message.getFeedbacks().size()) + 1;
        String promptFileName = getPromptFile(feedbackCount);
        String variable = llmClient.jsonEscape("이전 상황: " + context + "\nllm 질문: " + message.getLlmQuestion() + "\n사용자 답변: " + request.getUserAnswer());

        // 4. LLM 호출
        String prompt = llmClient.buildPrompt(promptFileName, variable);
        String llmResponse = llmClient.callChatGpt(prompt);

        // 5. LLM 응답 파싱
        String feedbackResult = llmClient.extractFieldValue(llmResponse, "feedbackResult");   // "GOOD" or "NEEDS_CORRECTION"
        String feedbackText = llmClient.extractFieldValue(llmResponse, "feedbackText");       // 피드백 설명 텍스트

        // 6. Feedback 엔티티 생성 및 저장
        ConversationFeedback feedback = converter.toConversationFeedback(request, feedbackResult, feedbackText, feedbackCount, message);
        feedbackRepo.save(feedback);

        // 7. message 엔티티 업데이트
        message.getFeedbacks().add(feedback);
        message.setLlmAnswer(feedbackText);
        messageRepo.save(message);

        // 8. Good 피드백인 경우
        if (feedback.isCorrect()) {
            ConversationSession.ConversationStep currentStep = message.getSession().getCurrentStep();

            if (currentStep == ConversationSession.ConversationStep.END) {
                // END 단계인 경우 비동기 호출 없음
            } else {

                // 다음 단계 계산
                ConversationSession.ConversationStep[] steps = ConversationSession.ConversationStep.values();
                int nextStepIndex = currentStep.ordinal() + 1;

                // 비동기로 next step을 진행
                ConversationSession.ConversationStep nextStep = steps[nextStepIndex];
                asyncService.prepareNextStep(message.getSession().getId(), nextStep);
            }

        }

        // 9. 응답 DTO 생성
        return ConversationResponseDto.FeedbackResponseDto.builder()
                .feedbackResult(feedbackResult)
                .feedbackText(feedbackText)
                .currentStep(message.getSession().getCurrentStep())
                .tryNum(feedbackCount)
                .build();
    }

    private String getPromptFile(int tryCount) {
        switch (tryCount) {
            case 1:
                return "feedback_try01.json";
            case 2:
                return "feedback_try02.json";
            case 3:
                return "feedback_try03.json";
            default:
                throw new CustomException(ErrorStatus.COMMON_BAD_REQUEST);
        }

    }

}