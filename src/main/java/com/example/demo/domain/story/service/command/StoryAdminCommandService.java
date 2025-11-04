package com.example.demo.domain.story.service.command;

import com.example.demo.domain.story.entity.Story;

public interface StoryAdminCommandService {

    // 이미지 유튜브 업로드
    Story uploadImageYoutube(Long storyId, String youtubeLink);

    // 동영상 유튜브 업로드
    Story uploadVideoYoutube(Long storyId, String youtubeLink);

}