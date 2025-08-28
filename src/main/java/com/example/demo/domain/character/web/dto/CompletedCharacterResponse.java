package com.example.demo.domain.character.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedCharacterResponse {

    private Long characterId;
    private String name;
    private String gender;
    private String imageUrl;
    private boolean important;   // 관심 캐릭터 여부
    private LocalDateTime createTime;

}