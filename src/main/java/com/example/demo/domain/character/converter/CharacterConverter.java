package com.example.demo.domain.character.converter;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.story.entity.StoryPage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CharacterConverter {

    // 목록용 변환
    public CompletedCharacterResponse toCompletedCharacterResponse(StoryCharacter ch, boolean important) {
        return toCompletedCharacterResponse(ch, important, false);
    }

    // 목록용 변환 (withCreator: 만든 사용자 닉네임 포함 - 학급 동화 조회에서 사용)
    public CompletedCharacterResponse toCompletedCharacterResponse(StoryCharacter ch, boolean important, boolean withCreator) {
        return CompletedCharacterResponse.builder()
                .characterId(ch.getId())
                .name(ch.getName())
                .gender(ch.getGender().name())
                .imageUrl(ch.getImageUrl())
                .important(important)
                .createTime(ch.getCreatedAt())
                .creatorNickname(withCreator ? creatorNickname(ch) : null)
                .build();
    }

    // 캐릭터(동화)를 만든 사용자 닉네임 (스토리의 사용자가 없으면 null)
    private String creatorNickname(StoryCharacter ch) {
        return ch.getStory() != null && ch.getStory().getUser() != null
                ? ch.getStory().getUser().getNickname()
                : null;
    }

    // 상세용 변환
    public CompletedCharacterResponse.Detail toDetailCharacterResponse(StoryCharacter ch, boolean important) {

        // 스토리 마지막 페이지(4p) 가져오기
        StoryPage lastPage = ch.getStory().getStoryPages().stream()
                .filter(sp -> sp.getPageNumber() == 4) // 항상 4p가 마지막
                .findFirst()
                .orElse(null);

        return CompletedCharacterResponse.Detail.builder()
                .characterId(ch.getId())
                .name(ch.getName())
                .gender(ch.getGender().name())
                .age(ch.getAge())
                .imageUrl(ch.getImageUrl())
                .personality(ch.getPersonality())
                .important(important)
                .createTime(ch.getCreatedAt())
                .creatorNickname(creatorNickname(ch))
                .storyId(ch.getStory().getId())
                .storyTitle(ch.getStory().getTitle())
                .imageStoryUrl(lastPage != null ? lastPage.getImageUrl() : null) // 4p 이미지 URL
                .videoStatus(ch.getStory().getVideoStatus()) // 비디오 생성 상태
                .videoStoryUrl(lastPage != null ? lastPage.getVideoUrl() : null) // 4p 비디오 URL
                .imageYoutubeLink(ch.getStory().getImageYoutubeLink())
                .videoYoutubeLink(ch.getStory().getVideoYoutubeLink())
                .build();
    }

    // List<StoryCharacter> → CompletedCharacterResponse.CharacterListResponse
    public CompletedCharacterResponse.CharacterListResponse toCharacterListResponse(
            List<StoryCharacter> characters,
            Set<Long> favoriteIds
    ) {
        return toCharacterListResponse(characters, favoriteIds, false);
    }

    // withCreator: 만든 사용자 닉네임 포함 (학급 동화 조회에서 사용)
    public CompletedCharacterResponse.CharacterListResponse toCharacterListResponse(
            List<StoryCharacter> characters,
            Set<Long> favoriteIds,
            boolean withCreator
    ) {
        List<CompletedCharacterResponse> dtoList = characters.stream()
                .map(ch -> toCompletedCharacterResponse(ch, favoriteIds.contains(ch.getId()), withCreator))
                .toList();

        return CompletedCharacterResponse.CharacterListResponse.builder()
                .characters(dtoList)
                .build();
    }

}