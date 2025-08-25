package com.example.demo.domain.story.converter;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.entity.StoryPage;
import com.example.demo.domain.story.web.dto.StoryPageResponseDto;
import com.example.demo.domain.story.web.dto.StoryResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public class StoryConverter {

    // Story → StoryItem 변환
    public static StoryResponseDto.StoryItem toStoryItem(Story story, ConversationSession.ConversationStep currentStep) {
        boolean isCompleted = story.getStatus() == Story.StoryStatus.COMPLETED;
        return StoryResponseDto.StoryItem.builder()
                .storyId(story.getId())
                .title(story.getTitle())
                .description(story.getDescription())
                .completed(isCompleted)
                .canContinue(!isCompleted)
                .sessionId(!isCompleted && !story.getStorySessions().isEmpty()
                        ? story.getStorySessions().stream().findFirst().get().getId()
                        : null)
                .currentStep(!isCompleted ? currentStep : null)
                .build();
    }

    // Page<Story> + 변환된 List<StoryItem> → StoryResponseDto
    public static StoryResponseDto toStoryResponseDto(Page<Story> storyPage, List<StoryResponseDto.StoryItem> storyItems) {
        return StoryResponseDto.builder()
                .stories(storyItems)
                .currentPage(storyPage.getNumber())
                .totalPages(storyPage.getTotalPages())
                .totalElements(storyPage.getTotalElements())
                .hasNext(storyPage.hasNext())
                .build();
    }

    // StoryPage → StoryPageResponseDto 변환
    public static StoryPageResponseDto toStoryPageResponseDto(StoryPage storyPage) {
        return StoryPageResponseDto.builder()
                .pageNumber(storyPage.getPageNumber())
                .content(storyPage.getContent())
                .imageUrl(storyPage.getImageUrl())
                .build();
    }

}