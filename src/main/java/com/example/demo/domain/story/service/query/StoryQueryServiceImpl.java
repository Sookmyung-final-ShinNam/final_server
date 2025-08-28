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

        // 1. 페이지 정보(Pageable) 생성
        Pageable pageable = PageRequest.of(page, size);

        // 2. 해당 유저의 스토리를 상태/즐겨찾기/생성일 순으로 페이징 조회
        Page<Story> storyPage = storyRepository.findAllByUserWithFavoriteOrderByStatusAndFavorite(user, pageable);

        // 3. 각 스토리를 StoryItem DTO로 변환
        List<StoryResponseDto.StoryItem> storyItems = storyPage.stream()
                .map(story -> {
                    ConversationSession.ConversationStep currentStep = null;

                    // 3-1. 진행 중인 스토리인 경우 현재 대화 단계 조회
                    if (story.getStatus() == Story.StoryStatus.IN_PROGRESS) {
                        currentStep = story.getStorySessions().stream()
                                .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                                .map(ConversationSession::getCurrentStep)
                                .orElse(ConversationSession.ConversationStep.START);
                    }

                    // 3-2. 관심 동화 여부 체크
                    boolean isFavorite = story.getUserStoryFavorites().stream()
                            .anyMatch(fav -> fav.getUser().getId().equals(user.getId()));

                    // 3-3. 첫 장 이미지 URL 조회
                    String firstImageUrl = story.getStoryPages().stream()
                            .filter(p -> p.getPageNumber() == 1)
                            .map(StoryPage::getImageUrl)
                            .findFirst()
                            .orElse(null);

                    // 3-4. DTO 변환
                    return StoryConverter.toStoryItem(story, currentStep, isFavorite, firstImageUrl);
                })
                .toList();

        // 4. 페이징 정보를 포함한 최종 응답 DTO 반환
        return StoryConverter.toStoryResponseDto(storyPage, storyItems);
    }

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