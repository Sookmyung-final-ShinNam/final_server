package com.example.demo.domain.story.web.dto;

import com.example.demo.domain.story.entity.Story;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryAdminResponseDto {

    private Long id;
    private String title;
    private String status;
    private String videoStatus;
    private String imageYoutubeLink;
    private String videoYoutubeLink;
    private boolean needsImageLink;
    private boolean needsVideoLink;

    public static StoryAdminResponseDto from(Story story) {
        boolean needsImage = story.getStatus() == Story.StoryStatus.COMPLETED &&
                (story.getImageYoutubeLink() == null || story.getImageYoutubeLink().isBlank());

        boolean needsVideo = story.getStatus() == Story.StoryStatus.COMPLETED &&
                story.getVideoStatus() == com.example.demo.domain.story.entity.StoryPage.VideoStatus.COMPLETED &&
                (story.getVideoYoutubeLink() == null || story.getVideoYoutubeLink().isBlank());

        return StoryAdminResponseDto.builder()
                .id(story.getId())
                .title(story.getTitle())
                .status(story.getStatus().name())
                .videoStatus(story.getVideoStatus().name())
                .imageYoutubeLink(story.getImageYoutubeLink())
                .videoYoutubeLink(story.getVideoYoutubeLink())
                .needsImageLink(needsImage)
                .needsVideoLink(needsVideo)
                .build();
    }

}