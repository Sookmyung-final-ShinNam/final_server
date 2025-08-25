package com.example.demo.domain.story.service.query;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.web.dto.StoryResponseDto;
import com.example.demo.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryQueryServiceImpl implements  StoryQueryService {

    private final StoryRepository storyRepository;

    @Override
    @Transactional
    public List<StoryResponseDto> getPagedStories(int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size); // 정렬은 Repository에서 처리
        Page<Story> storyPage = storyRepository.findAllByUserWithFavoriteOrderByStatusAndFavorite(user, pageable);

        return storyPage.stream()
                .map(story -> {
                    ConversationSession.ConversationStep currentStep = null;
                    if (story.getStatus() != Story.StoryStatus.COMPLETED) {
                        currentStep = story.getStorySessions().stream()
                                .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                                .map(ConversationSession::getCurrentStep)
                                .orElse(ConversationSession.ConversationStep.START);
                    }
                    return StoryResponseDto.fromEntity(story, currentStep);
                })
                .collect(Collectors.toList());
    }

}