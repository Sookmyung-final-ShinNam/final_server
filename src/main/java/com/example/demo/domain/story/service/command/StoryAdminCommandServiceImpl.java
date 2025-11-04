package com.example.demo.domain.story.service.command;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryAdminCommandServiceImpl implements StoryAdminCommandService {

    private final StoryRepository storyRepository;

    @Transactional
    public Story uploadImageYoutube(Long id, String youtubeLink) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        story.setImageYoutubeLink(youtubeLink);
        return storyRepository.save(story);
    }

    @Transactional
    public Story uploadVideoYoutube(Long id, String youtubeLink) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        story.setVideoYoutubeLink(youtubeLink);
        return storyRepository.save(story);
    }

}