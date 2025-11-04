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
    public String uploadImageYoutube(Long id, String youtubeLink) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        story.setImageYoutubeLink(youtubeLink);
        storyRepository.save(story);

        return  "성공적으로 동영상 유튜브 링크가 업로드되었습니다. : " + youtubeLink;
    }

    @Transactional
    public String uploadVideoYoutube(Long id, String youtubeLink) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        story.setVideoYoutubeLink(youtubeLink);
        storyRepository.save(story);

        return "성공적으로 동영상 유튜브 링크가 업로드되었습니다. : " + youtubeLink;
    }

}