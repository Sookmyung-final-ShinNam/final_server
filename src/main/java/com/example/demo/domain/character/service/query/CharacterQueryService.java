package com.example.demo.domain.character.service.query;

import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;

public interface CharacterQueryService {

    /**
     * 완성된 캐릭터들에 대한 페이징 조회
     * @param user 현재 로그인한 사용자
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return CharacterListResponse - 페이징된 완성된 캐릭터 목록
     */
    CompletedCharacterResponse.CharacterListResponse getCompletedCharacters(User user, int page, int size);

    /**
     * 단일 캐릭터 상세 조회
     * @param user 현재 로그인한 사용자
     * @param characterId 조회할 캐릭터의 ID
     * @return CompletedCharacterResponse.Detail - 캐릭터 상세 정보
     */
    CompletedCharacterResponse.Detail getCharacterDetail(User user, Long characterId);
}