package com.example.demo.domain.character.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedCharacterResponse {

    // 전체 조회용 캐릭터 정보
    private Long characterId;
    private String name;
    private String gender;
    private String imageUrl;
    private boolean important;   // 관심 캐릭터 여부
    private LocalDateTime createTime;

    // 전체 조회용 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterListResponse {
        private List<CompletedCharacterResponse> characters;
    }

    // 상세 조회용 내부 DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        private Long characterId;
        private String name;
        private String gender;
        private int age;
        private String imageUrl;
        private String personality;
        private boolean important;
        private LocalDateTime createTime;

        private Long storyId;               // 연관 스토리 ID
        private String storyTitle;          // 연관 스토리 제목
    }

}