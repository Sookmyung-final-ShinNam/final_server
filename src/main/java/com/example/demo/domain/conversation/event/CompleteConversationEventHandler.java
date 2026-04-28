package com.example.demo.domain.conversation.event;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.conversation.service.command.ConversationCompleteMediaCommandService;
import com.example.demo.domain.conversation.service.command.ConversationCompleteTextCommandService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompleteConversationEventHandler {

    private final StoryRepository storyRepo;
    private final ConversationCompleteTextCommandService conversationCompleteTextCommandService;
    private final ConversationCompleteMediaCommandService conversationCompleteMediaCommandService;
    private final ConversationCompleteCommandService conversationCompleteCommandService;

    /**
     * 스토리 생성 이벤트 핸들러
     * - 현재 스토리 상태에 따라 필요 작업 수행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompleteConversationEvent event) {

        Long storyId = event.getStoryId();
        log.info("[START EVENT(CompleteConversationEvent)] storyId={}", storyId);

        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 1. 이미 완료된 스토리 작업 생략
        if (story.getStatus().isCompletedStatus()) {
            log.info("이미 이미지까지 생성된 스토리: storyId={}", storyId);
            return;
        }

        // 2. 스토리 정제: LLM 호출 및 Story/Character/StoryPage 업데이트
        if (story.getStatus() == Story.StoryStatus.MAKING ||
                story.getStatus() == Story.StoryStatus.TEXT_FAILED
        ) {
            try {
                conversationCompleteTextCommandService.completeStoryFromLlm(storyId, event.getSessionId());
                story = storyRepo.findById(storyId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND)); // 스토리 최신 상태 재조회
            } catch (Exception e) {
                // 스토리 정제 실패 처리
                log.error("[END EVENT(CompleteConversationEvent)] 스토리 정제 실패 storyId={}", storyId, e);
                conversationCompleteCommandService.failStory(storyId, Story.StoryStatus.TEXT_FAILED);
                return;
            }

        }

        // 3. 이미지 생성: 캐릭터 및 페이지 이미지 생성
        if (story.getStatus() == Story.StoryStatus.TEXT_COMPLETED ||
                story.getStatus() == Story.StoryStatus.IMAGE_FAILED
        ) {
            try {
                conversationCompleteMediaCommandService.generateStoryMedia(storyId, "image");
            } catch (Exception e) {
                // 이미지 생성 실패 처리
                log.error("[END EVENT(CompleteConversationEvent)] 이미지 생성 실패 storyId={}", storyId, e);
                conversationCompleteCommandService.failStory(storyId, Story.StoryStatus.IMAGE_FAILED);
                return;
            }
        }

        log.info("[END EVENT(CompleteConversationEvent)] storyId={}", storyId);
    }
}