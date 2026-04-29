package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.web.dto.StoryAdminResponseDto;
import com.example.demo.domain.story.web.dto.StoryFailedAdminResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryAdminQueryServiceImpl implements StoryAdminQueryService {

    private final StoryRepository storyRepository;

    @Override
    public List<StoryAdminResponseDto> getIncompleteStories() {
        return storyRepository.findIncompleteStoriesForAdmin()
                .stream()
                .map(StoryAdminResponseDto::from)
                .toList();
    }

    // 스토리 재생성 배치 실패 동화 조회
    @Override
    public List<StoryFailedAdminResponseDto> getFailedRetryStories() {
        return storyRepository.findFailedRetryStoriesForAdmin(
                    Arrays.asList(
                        Story.StoryStatus.IMAGE_COMPLETED,
                        Story.StoryStatus.VIDEO_COMPLETED
                    )
                )
                .stream()
                .map(StoryFailedAdminResponseDto::from)
                .toList();
    }
}