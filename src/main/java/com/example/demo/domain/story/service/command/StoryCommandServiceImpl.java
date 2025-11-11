package com.example.demo.domain.story.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoryCommandServiceImpl implements StoryCommandService {

    private final StoryRepository storyRepository;

    @Override
    @Transactional
    public void markStoryVideoAsMaking(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 유저 포인트 사용
        story.getUser().usePoints(1);

        story.markVideoAsMaking();  // 엔티티 내부에서 상태 변경
        storyRepository.save(story); // DB 반영
    }

}