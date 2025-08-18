package com.example.demo.domain.conversation.web.dto;

import com.example.demo.domain.character.entity.StoryCharacter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ConversationRequestDto {

    @Getter
    @NoArgsConstructor
    public static class ConversationStartRequestDto {

        @NotEmpty(message = "배경 이름은 필수입니다.")
        private String backgroundName;

        @NotEmpty(message = "테마는 최소 한 개 이상 필요합니다.")
        private List<@NotEmpty(message = "테마 이름은 비어있을 수 없습니다.") String> themeNames;

        @NotEmpty(message = "characterName은 필수입니다.")
        private String characterName;

        @NotNull(message = "characterAge는 필수입니다.")
        private Integer characterAge;

        @NotNull(message = "gender는 필수입니다.")
        private StoryCharacter.Gender gender;

        @NotEmpty(message = "eyeColor는 필수입니다.")
        private String eyeColor;

        @NotEmpty(message = "hairColor는 필수입니다.")
        private String hairColor;

        @NotEmpty(message = "hairStyle은 필수입니다.")
        private String hairStyle;

    }

}