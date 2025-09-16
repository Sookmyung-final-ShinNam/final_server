package com.example.demo.domain.story.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.story.converter.StoryConverter;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryQueryServiceImpl implements StoryQueryService {

    private final StoryRepository storyRepository;
    private final StoryPageRepository storyPageRepository;

    @Override
    @Transactional
    public StoryPageResponseDto getStoryPage(Long storyId, int pageNumber) {

        if (pageNumber < 0 || pageNumber > 4) {
            throw new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND);
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        if (story.getStatus() != Story.StoryStatus.COMPLETED) {
            throw new CustomException(ErrorStatus.STORY_NOT_COMPLETED);
        }

        // 0페이지
        if (pageNumber == 0) {
            return StoryConverter.toStoryPageResponseDto(story, null, 0);
        }

        // 1~4페이지
        StoryPage storyPage = storyPageRepository.findByStory_IdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND));

        return StoryConverter.toStoryPageResponseDto(story, storyPage, pageNumber);
    }

}