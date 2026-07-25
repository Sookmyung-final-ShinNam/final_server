package com.example.demo.domain.story.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.event.GenerateVideoEvent;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryCommandServiceImpl implements StoryCommandService {

    private final StoryRepository storyRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void generateVideo(Long storyId) {

        // 1. Story 조회
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        if (!story.getStoryStatus().isCompletedStory()) {
            throw new CustomException(ErrorStatus.STORY_NOT_COMPLETED);     // 완성된 동화인지 확인
        }

        Story.VideoStatus prevStatus = story.getVideoStatus();
        if (prevStatus.isCompletedVideo()) {
            throw new CustomException(ErrorStatus.VIDEO_ALREADY_COMPLETED); // 완성된 동영상인지 확인
        }

        // 2. 스토리 상태 벌크 업데이트 (NONE/VIDEO_FAILED -> VIDEO_MAKING)
        int updated = storyRepository.updateStatusAsMaking(
                storyId,
                Arrays.asList(
                        Story.VideoStatus.NONE,
                        Story.VideoStatus.VIDEO_FAILED
                )
        );

        if (updated == 0) {
            log.info("[Video] 이미 동영상 생성 중. storyId={}", storyId);
            return; // 이미 VIDEO_MAKING 상태라면 중복 요청 무시
        }

        // 3. 최초 생성 요청(NONE)일 때만 포인트 차감
        if (prevStatus == Story.VideoStatus.NONE) {
            story.getUser().usePoints(1);
        }

        // 4. 비동기 동영상 생성 이벤트 발행
        eventPublisher.publishEvent(
                new GenerateVideoEvent(storyId)
        );
    }
}