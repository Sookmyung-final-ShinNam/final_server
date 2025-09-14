package com.example.demo.global.web.dto;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class HomeResponse {

    private Long userId;
    private String username;
    private String profileImageUrl;
    private List<CharacterDto> favorites;

    public static HomeResponse from(User user, List<StoryCharacter> favorites) {
        return HomeResponse.builder()
                .userId(user.getId())
                .username(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .favorites(favorites.stream()
                        .map(CharacterDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    // 내부 DTO
    @Getter
    @Builder
    public static class CharacterDto {
        private Long id;
        private String name;
        private String imageUrl;

        public static CharacterDto from(StoryCharacter character) {
            return CharacterDto.builder()
                    .id(character.getId())
                    .name(character.getName())
                    .imageUrl(character.getImageUrl())
                    .build();
        }
    }

}