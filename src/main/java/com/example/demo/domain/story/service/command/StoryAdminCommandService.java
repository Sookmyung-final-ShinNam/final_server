package com.example.demo.domain.story.service.command;

public interface StoryAdminCommandService {

    // 이미지 유튜브 업로드
    String uploadImageYoutube(Long storyId, String youtubeLink);

    // 동영상 유튜브 업로드
    String uploadVideoYoutube(Long storyId, String youtubeLink);

}