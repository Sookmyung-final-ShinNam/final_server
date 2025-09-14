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
public class StoryQueryServiceImpl implements  StoryQueryService {

    private final StoryRepository storyRepository;
    private final StoryPageRepository storyPageRepository;

    @Override
    @Transactional
    public StoryPageResponseDto getStoryPage(Long storyId, int pageNumber) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_NOT_FOUND));

        // 1. 동화 상태 체크 (완성된 것만 허용)
        if (story.getStatus() != Story.StoryStatus.COMPLETED) {
            throw new CustomException(ErrorStatus.STORY_NOT_COMPLETED);
        }

        // 2. 페이지 조회
        StoryPage page = storyPageRepository.findByStory_IdAndPageNumber(storyId, pageNumber)
                .orElseThrow(() -> new CustomException(ErrorStatus.STORY_PAGE_NOT_FOUND));

        // 3. 첫 장 또는 마지막 장인지 체크 → 캐릭터 정보 포함 여부 결정
        boolean includeCharacter = (pageNumber == 1 || pageNumber == 4);

        return StoryConverter.toStoryPageResponseDto(page, includeCharacter);
    }

}