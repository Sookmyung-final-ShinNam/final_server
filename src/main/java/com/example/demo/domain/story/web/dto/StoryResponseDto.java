package com.example.demo.domain.story.web.dto;

import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.entity.Story;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponseDto {

    private Long storyId;
    private String title;
    private String description;
    private boolean completed;      // 완료 여부
    private boolean canContinue;    // 미완성이면 이어하기 가능
    private ConversationSession.ConversationStep currentStep;     // 이어하기 가능한 경우 현재 단계

    public static StoryResponseDto fromEntity(Story story, ConversationSession.ConversationStep currentStep) {
        boolean isCompleted = story.getStatus() == Story.StoryStatus.COMPLETED;
        return StoryResponseDto.builder()
                .storyId(story.getId())
                .title(story.getTitle())
                .description(story.getDescription())
                .completed(isCompleted)
                .canContinue(!isCompleted)
                .currentStep(!isCompleted ? currentStep : null) // 완료된 스토리는 null
                .build();
    }

}