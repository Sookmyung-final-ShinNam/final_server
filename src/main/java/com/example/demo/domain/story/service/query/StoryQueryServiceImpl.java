package com.example.demo.domain.story.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.converter.StoryConverter;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.repository.StoryPageRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;
import com.example.demo.domain.story.web.dto.StoryResponseDto;
import com.example.demo.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryQueryServiceImpl implements  StoryQueryService {

    private final StoryRepository storyRepository;
    private final StoryPageRepository storyPageRepository;

    @Override
    @Transactional
    public StoryResponseDto getPagedStories(int page, int size, User user) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findAllByUserWithFavoriteOrderByStatusAndFavorite(user, pageable);

        List<StoryResponseDto.StoryItem> storyItems = storyPage.stream()
                .map(story -> {
                    ConversationSession.ConversationStep currentStep = null;
                    if (story.getStatus() == Story.StoryStatus.IN_PROGRESS) {
                        currentStep = story.getStorySessions().stream()
                                .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                                .map(ConversationSession::getCurrentStep)
                                .orElse(ConversationSession.ConversationStep.START);
                    }
                    return StoryConverter.toStoryItem(story, currentStep);
                })
                .toList();

        return StoryConverter.toStoryResponseDto(storyPage, storyItems);
    }

    @Override
    @Transactional
    public StoryPageResponseDto getStoryPage(Long storyId, int pageNumber) {
        StoryPage page = storyPageRepository.findByStory_IdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND));
        return StoryConverter.toStoryPageResponseDto(page);
    }

}