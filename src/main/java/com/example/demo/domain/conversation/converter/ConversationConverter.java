package com.example.demo.domain.conversation.converter;

import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.entity.ConversationFeedback;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.story.entity.*;
import com.example.demo.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ConversationConverter {

    public Story toStory(User user) {
        return Story.builder()
                .user(user)
                .status(Story.StoryStatus.IN_PROGRESS)
                .videoStatus(StoryPage.VideoStatus.NONE)
                .build();
    }

    public StoryTheme toStoryTheme(Story story, Theme theme) {
        return StoryTheme.builder()
                .story(story)
                .theme(theme)
                .build();
    }

    public StoryBackground toStoryBackground(Story story, Background background) {
        return StoryBackground.builder()
                .story(story)
                .background(background)
                .build();
    }

    public StoryCharacter toStoryCharacter(Story story, ConversationRequestDto.ConversationStartRequestDto request) {
        return StoryCharacter.builder()
                .story(story)
                .name(request.getCharacterName())
                .age(request.getCharacterAge())
                .gender(request.getGender())
                .status(StoryCharacter.CharacterStatus.IN_PROGRESS)
                .build();
    }

    public CharacterAppearance toCharacterAppearance(StoryCharacter character, ConversationRequestDto.ConversationStartRequestDto request) {
        return CharacterAppearance.builder()
                .character(character)
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .hairStyle(request.getHairStyle())
                .build();
    }

    public ConversationSession toConversationSession(Story story, User user) {
        return ConversationSession.builder()
                .story(story)
                .user(user)
                .currentStep(ConversationSession.ConversationStep.START)
                .build();
    }

    public ConversationMessage toConversationMessage(ConversationSession session, String nextStory) {
        return ConversationMessage.builder()
                .session(session)
                .nextStory(nextStory)
                .build();
    }

    public ConversationMessage toConversationMessage(String nextStory, String llmQuestion, ConversationSession session) {
        return ConversationMessage.builder()
                .session(session)
                .nextStory(nextStory)
                .llmQuestion(llmQuestion)
                .build();
    }

    public ConversationResponseDto.NextStepResponseDto toNextStepResponseDto(ConversationMessage message) {
        return ConversationResponseDto.NextStepResponseDto.builder()
                .messageId(message.getId())
                .nextStory(message.getNextStory())
                .llmQuestion(message.getLlmQuestion())
                .build();
    }

    public ConversationFeedback toConversationFeedback(
            String userAnswer,
            String feedbackResult,
            String feedbackText,
            int feedbackCount,
            ConversationMessage message
    ) {
        return ConversationFeedback.builder()
                .attemptNumber(feedbackCount)
                .userAnswer(userAnswer)
                .isCorrect("GOOD".equalsIgnoreCase(feedbackResult))
                .feedbackText(feedbackText)
                .message(message)
                .build();
    }

}