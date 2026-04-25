package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.StoryCompletedEvent;
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

    private final ConversationSessionRepository sessionRepo;
    private final StoryRepository storyRepo;

    private final ConversationCompleteCommandService conversationCompleteCommandService;
    private final ApplicationEventPublisher eventPublisher;

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