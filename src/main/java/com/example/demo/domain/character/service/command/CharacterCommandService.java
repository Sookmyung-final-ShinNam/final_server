package com.example.demo.domain.character.service.command;

import com.example.demo.domain.user.entity.User;

public interface CharacterCommandService {

    /**
     * 캐릭터를 관심 캐릭터 목록에 등록
     * @param user 현재 로그인한 사용자
     * @param characterId 등록할 캐릭터 ID
     */
    String addFavorite(User user, Long characterId);

    /**
     * 캐릭터를 관심 캐릭터 목록에서 제거
     * @param user 현재 로그인한 사용자
     * @param characterId 제거할 캐릭터 ID
     */
    String removeFavorite(User user, Long characterId);

}