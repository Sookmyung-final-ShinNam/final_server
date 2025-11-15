package com.example.demo.domain.conversation.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.service.query.ConversationQueryService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/conversations")
@RequiredArgsConstructor
public class ConversationTestController {

    private final ConversationMessageRepository messageRepo;
    private final ConversationSessionRepository sessionRepo;
    private final StoryRepository storyRepo;

    private final ConversationQueryService conversationQueryService;
    private final ConversationCompleteCommandService conversationCompleteCommandService;
    private final ConversationConverter converter;
    private final LlmClient llmClient;


    @PostMapping("/complete")
    public ApiResponse<Void> storyCompleteTest(@RequestParam Long sessionId) {
        // 1. Story 조회
        Story story = storyRepo.findByStorySessions_Id(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        // 2. 현재 단계가 END 인지 확인
        if (session.getCurrentStep() != ConversationSession.ConversationStep.END) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 3. 마지막 메시지 조회
        ConversationMessage lastMessage = session.getMessages().isEmpty()
                ? null
                : session.getMessages().get(session.getMessages().size() - 1);

        if (lastMessage == null || lastMessage.getLlmAnswer() == null) {
            throw new CustomException(ErrorStatus.SESSION_INVALID_STATE);
        }

        // 4. 상태 변경 -> MAKING 에서는 이어하기 불가
        markStoryMaking(story); // 중간 트랜젝션 처리

        // 5. 이전 대화 조회
        String context = "";
        if (!story.getStorySessions().isEmpty()) {
            context = conversationQueryService.findSessionContextById(session.getId());
        } else {
            throw new CustomException(ErrorStatus.STORY_NOT_FOUND);
        }

        // 6. LLM 호출 및 Story/Character/StoryPage 업데이트
        conversationCompleteCommandService.completeStoryFromLlm(story, context);

        // 7. 캐릭터 및 StoryPage 이미지 생성
        conversationCompleteCommandService.generateStoryMedia(story.getId(), "image");

        // 8. 최종 상태 업데이트
        story.setStatus(Story.StoryStatus.COMPLETED);
        story.getCharacter().setStatus(StoryCharacter.CharacterStatus.COMPLETED);

        System.out.println("비동기 작업 완료: storyStatus=" + story.getStatus());

        return ApiResponse.of(SuccessStatus._OK);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markStoryMaking(Story story) {
        story.setStatus(Story.StoryStatus.MAKING);
    }

}