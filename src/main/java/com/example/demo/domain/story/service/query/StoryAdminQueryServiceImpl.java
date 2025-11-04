package com.example.demo.domain.story.service.query;

import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.story.web.dto.StoryAdminResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}