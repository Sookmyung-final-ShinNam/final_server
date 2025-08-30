package com.example.demo.domain.story.service.command;

import com.example.demo.domain.user.entity.User;

public interface StoryCommandService {

    /**
     * 동화를 관심 동화 목록에 등록
     * @param user 현재 로그인한 사용자
     * @param storyId 등록할 동화 ID
     */
    String addFavorite(User user, Long storyId);

    /**
     * 동화를 관심 동화 목록에서 제거
     * @param user 현재 로그인한 사용자
     * @param storyId 제거할 동화 ID
     */
    String removeFavorite(User user, Long storyId);

}