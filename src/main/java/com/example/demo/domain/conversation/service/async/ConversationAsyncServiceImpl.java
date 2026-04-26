package com.example.demo.domain.conversation.service.async;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.command.ConversationCompleteCommandService;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAsyncServiceImpl implements ConversationAsyncService {

    private final ConversationSessionRepository sessionRepo;
    private final StoryRepository storyRepo;

    private final ConversationCompleteCommandService conversationCompleteCommandService;

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

        if (story.getStatus() == Story.StoryStatus.MAKING) {
            // 3. 이전 대화 조회
            ConversationSession session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

            // 2. LLM 호출 및 Story/Character/StoryPage 업데이트 (스토리 정제)
            conversationCompleteCommandService.completeStoryFromLlm(storyId, session.getFullStory());

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