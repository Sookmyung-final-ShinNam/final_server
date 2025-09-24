package com.example.demo.domain.conversation.service.command;

import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationMessage;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.event.ConversationStartedEvent;
import com.example.demo.domain.conversation.repository.ConversationMessageRepository;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.story.entity.*;
import com.example.demo.domain.story.repository.*;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationStartCommandServiceImpl implements ConversationStartCommandService {

    private final ThemeRepository themeRepository;
    private final BackgroundRepository backgroundRepository;
    private final StoryRepository storyRepository;
    private final StoryThemeRepository storyThemeRepository;
    private final StoryBackgroundRepository storyBackgroundRepository;
    private final StoryCharacterRepository storyCharacterRepository;
    private final CharacterAppearanceRepository characterAppearanceRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    private final LlmClient llmClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationConverter converter;


    /**
     * 대화 세션 시작 요청을 처리하고,
     * Story, Character, Session 생성 및 LLM 호출을 통해 첫 문장 생성 후 결과 반환
     *
     * @param user    대화 요청을 보낸 사용자
     * @param request 대화 시작 요청 DTO
     * @return 시작 응답 DTO (sessionId, nextStory)
     */
    @Override
    @Transactional
    public ConversationResponseDto.ConversationStartResponseDto startConversation(
            ConversationRequestDto.ConversationStartRequestDto request,
            User user
    ) {

        // 사용자와 연관된 새 Story 엔티티 생성 (상태는 IN_PROGRESS)
        Story story = storyRepository.save(converter.toStory(user));

        // 테마 이름 리스트로부터 기존 테마 조회 또는 새로 생성
        List<Theme> themes = getOrCreateThemes(request.getThemeNames());

        // 생성된 Story에 테마들 연결 (StoryTheme 엔티티 생성)
        for (Theme theme : themes) {
            StoryTheme storyTheme = converter.toStoryTheme(story, theme);
            storyThemeRepository.save(storyTheme);
            story.getStoryThemes().add(storyTheme);
        }

        // 배경 이름으로 기존 배경 조회 또는 새로 생성
        Background background = getOrCreateBackground(request.getBackgroundName());

        // 생성된 Story에 배경 연결 (StoryBackground 엔티티 생성)
        StoryBackground storyBackground = converter.toStoryBackground(story, background);
        storyBackgroundRepository.save(storyBackground);
        story.getStoryBackgrounds().add(storyBackground);

        // 요청 데이터를 기반으로 캐릭터 및 캐릭터 외형 정보 생성 후 Story와 연결
        StoryCharacter character = storyCharacterRepository.save(converter.toStoryCharacter(story, request));
        CharacterAppearance appearance = characterAppearanceRepository.save(converter.toCharacterAppearance(character, request));
        character.setAppearance(appearance);
        story.setCharacter(character);

        // Story와 User를 연결한 대화 세션 생성 (currentStep은 START)
        ConversationSession session = conversationSessionRepository.save(converter.toConversationSession(story, user));
        story.getStorySessions().add(session);

        // LLM 호출 준비
        String promptFileName = "gui_story_start.json";
        String variable = llmClient.jsonEscape(
                "동화 배경: " + background.getName() +
                ", 동화 테마: " + request.getThemeNames() +
                ", 캐릭터 이름: " + character.getName() +
                ", 캐릭터 나이: " + character.getAge() +
                ", 캐릭터 성별: " + character.getGender() +
                ", 캐릭터 눈 색: " + appearance.getEyeColor() +
                ", 캐릭터 머리 색:" + appearance.getHairColor() +
                ", 캐릭터 머리 스타일:" + appearance.getHairStyle()
        );

        // LLM 호출
        String promptJson = llmClient.buildPrompt(promptFileName, variable);
        String response = llmClient.callChatGpt(promptJson);

        // LLM 응답에서 첫 스토리 추출
        String startText = llmClient.extractFieldValue(response, "startText");

        // 생성된 첫 문장을 포함하는 ConversationMessage 저장
        ConversationMessage message = conversationMessageRepository.save(converter.toConversationMessage(session, startText));
        session.addMessage(message);

        // === EVENT: 비동기로 다음 단계 사전 생성 작업 실행 ===
        eventPublisher.publishEvent(
                new ConversationStartedEvent(session.getId(), ConversationSession.ConversationStep.STEP_01)
        );

        // 세션 ID와 생성된 첫 스토리를 포함하는 응답 DTO 반환
        return ConversationResponseDto.ConversationStartResponseDto.builder()
                .sessionId(session.getId())
                .nextStory(startText)
                .currentStep(ConversationSession.ConversationStep.START)
                .build();
    }

    // 요청받은 테마 이름으로 DB에서 조회하거나 없으면 신규 생성
    private List<Theme> getOrCreateThemes(List<String> themeNames) {
        List<Theme> themes = new ArrayList<>();
        for (String themeName : themeNames) {
            Theme theme = themeRepository.findByName(themeName)
                    .orElseGet(() -> themeRepository.save(
                            Theme.builder()
                                    .name(themeName)
                                    .build()
                    ));
            themes.add(theme);
        }
        return themes;
    }

    // 요청받은 배경 이름으로 DB에서 조회하거나 없으면 신규 생성
    private Background getOrCreateBackground(String backgroundName) {
        return backgroundRepository.findByName(backgroundName)
                .orElseGet(() -> backgroundRepository.save(
                        Background.builder()
                                .name(backgroundName)
                                .build()
                ));
    }

}