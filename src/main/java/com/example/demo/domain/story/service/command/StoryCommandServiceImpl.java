package com.example.demo.domain.story.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.UserStoryFavorite;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.repository.UserStoryFavoriteRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoryCommandServiceImpl implements StoryCommandService {

    private final StoryRepository storyRepository;
    private final UserStoryFavoriteRepository userStoryFavoriteRepository;

    @Override
    @Transactional
    public String addFavorite(User user, Long storyId) {

        // 동화가 존재하는지 확인
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 이미 관심 동화로 등록되어 있는지 확인
        if (userStoryFavoriteRepository.existsByUserAndStory(user, story)) {
            throw new CustomException(ErrorStatus.STORY_ALREADY_FAVORITE);
        }

        // 관심 동화로 등록
        UserStoryFavorite favorite = UserStoryFavorite.builder()
                .user(user)
                .story(story)
                .build();
        userStoryFavoriteRepository.save(favorite);

        return "관심 동화로 등록되었습니다.";
    }

    @Override
    @Transactional
    public String removeFavorite(User user, Long storyId) {

        // 동화가 존재하는지 확인
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 관심 동화로 등록되어 있는지 확인
        UserStoryFavorite favorite = userStoryFavoriteRepository.findByUserAndStory(user, story)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FAVORITE));

        // 관심 동화에서 제거
        userStoryFavoriteRepository.delete(favorite);

        return "관심 동화에서 제거되었습니다.";
    }

}