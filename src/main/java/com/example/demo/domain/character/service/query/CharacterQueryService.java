package com.example.demo.domain.character.service.query;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;

public interface CharacterQueryService {

    /**
     * 캐릭터 전체 조회
     * @param user   현재 로그인한 사용자
     * @param gender 성별 필터 (null이면 전체)
     * @return CharacterListResponse - 캐릭터들의 리스트
     */
    CompletedCharacterResponse.CharacterListResponse getCompletedCharacters(User user, StoryCharacter.Gender gender);

    /**
     * 단일 캐릭터 상세 조회
     * @param user 현재 로그인한 사용자
     * @param characterId 조회할 캐릭터의 ID
     * @return CompletedCharacterResponse.Detail - 캐릭터 상세 정보
     */
    CompletedCharacterResponse.Detail getCharacterDetail(User user, Long characterId);
}