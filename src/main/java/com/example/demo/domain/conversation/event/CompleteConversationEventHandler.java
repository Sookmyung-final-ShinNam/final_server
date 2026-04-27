package com.example.demo.domain.conversation.event;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
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
    private final ConversationCompleteCommandService conversationCompleteCommandService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CompleteConversationEvent event) {

        Long storyId = event.getStoryId();
        Story.StoryStatus storyStatus = getCurrentStatus(storyId);
        log.info("[START EVENT(CompleteConversationEvent)] storyId={}", storyId);

        // 1. 스토리 상태 조회
        if (storyStatus == Story.StoryStatus.READY_IMAGE ||
                storyStatus == Story.StoryStatus.READY_VIDEO) {
            log.info("이미 이미지까지 생성된 스토리: storyId={}", storyId);
            return;
        }

        // 2. 스토리 정제: LLM 호출 및 Story/Character/StoryPage 업데이트
        if (storyStatus == Story.StoryStatus.MAKING) {
            conversationCompleteCommandService.completeStoryFromLlm(storyId, event.getContext());
            storyStatus = getCurrentStatus(storyId); // 스토리 최신 상태 재조회
        }

        // 3. 이미지 생성: 캐릭터 및 페이지 이미지 생성
        if (storyStatus == Story.StoryStatus.COMPLETED) {
            conversationCompleteCommandService.generateStoryMedia(storyId, "image");
        }

        log.info("[END EVENT(CompleteConversationEvent)] storyId={}", storyId);
    }

    // 스토리 상태 조회
    private Story.StoryStatus getCurrentStatus(Long storyId) {
        Story story = storyRepo.findById(storyId).orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        return story.getStatus();
    }
}