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
import org.springframework.data.domain.Sort;
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
        Sort sort = Sort.by(
                Sort.Order.asc("status"),        // IN_PROGRESS 먼저
                Sort.Order.desc("important"),    // 중요도
                Sort.Order.desc("createdAt")     // 생성시간 최신순
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Story> storyPage = storyRepository.findAllByUser(user, pageable);

        return storyPage.stream()
                .map(story -> {
                    ConversationSession.ConversationStep currentStep = null;
                    if (story.getStatus() != Story.StoryStatus.COMPLETED) {
                        // 이어하기 가능한 경우, 가장 최근 ConversationSession의 currentStep를 가져오기
                        currentStep = story.getStorySessions().stream()
                                .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                                .map(s -> s.getCurrentStep())
                                .orElse(ConversationSession.ConversationStep.START); // 시작 단계
                    }
                    return StoryResponseDto.fromEntity(story, currentStep);
                })
                .collect(Collectors.toList());
    }

}