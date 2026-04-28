package com.example.demo.domain.conversation.service.command.start;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.*;
import com.example.demo.domain.conversation.event.StartConversationEvent;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.conversation.repository.SlotDefinitionRepository;
import com.example.demo.domain.conversation.service.model.llm.LlmClient;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.story.entity.*;
import com.example.demo.domain.story.repository.*;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationStartCommandServiceImpl implements ConversationStartCommandService {

    private final UserRepository userRepository;

    private final ThemeRepository themeRepository;
    private final BackgroundRepository backgroundRepository;

    private final StoryRepository storyRepository;
    private final StoryThemeRepository storyThemeRepository;
    private final StoryBackgroundRepository storyBackgroundRepository;

    private final StoryCharacterRepository storyCharacterRepository;
    private final CharacterAppearanceRepository characterAppearanceRepository;

    private final ConversationSessionRepository conversationSessionRepository;
    private final SlotDefinitionRepository slotDefinitionRepository;

    private final LlmClient llmClient;
    private final ApplicationEventPublisher eventPublisher;
    private final ConversationConverter converter;

    @Override
    @Transactional
    public ConversationResponseDto.ConversationStartResponseDto startConversation(
            ConversationRequestDto.ConversationStartRequestDto request,
            User user
    ) {

        // 1. 유저 검증 및 포인트 차감
        User currentUser = validateAndChargeUser(user);

        // 2. Story 생성
        Story story = storyRepository.save(converter.toStory(currentUser));

        // 3. Theme 처리 및 적용
        List<Theme> themes = resolveThemes(request);
        applyThemes(story, themes);

        // 4. Background 처리 및 적용
        Background background = resolveBackground(request);
        applyBackground(story, background);

        // 5. Character 생성
        StoryCharacter character = createCharacter(story, request);
        CharacterAppearance appearance = createAppearance(character, request);
        applyCharacter(story, character, appearance);

        // 6. Session 생성
        ConversationSession session = createSession(story, currentUser);

        // 7. LLM 호출 → 첫 시작 문장 생성
        String startText = generateStartText(background, character, appearance, request);

        // 8. Session 초기 구조 생성 (기/승/전/결 + Slot preload)
        initializeSessionState(session, startText);

        // 9. 기 단계 사전 생성 - 비동기
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(
                                new StartConversationEvent(session.getId())
                        );
                    }
                }
        );

        // 10. 세션 ID와 생성된 첫 스토리를 포함하는 응답 DTO 반환
        return ConversationResponseDto.ConversationStartResponseDto.builder()
                .sessionId(session.getId())
                .nextStory(startText)
                .currentStep(ConversationSession.ConversationStep.START)
                .build();
    }

    private User validateAndChargeUser(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        currentUser.usePoints(1);
        return currentUser;
    }

    private List<Theme> resolveThemes(ConversationRequestDto.ConversationStartRequestDto request) {
        return request.getThemeNames().stream()
                .map(name -> themeRepository.findByName(name)
                        .orElseGet(() -> themeRepository.save(
                                Theme.builder().name(name).build()
                        )))
                .toList();
    }

    private void applyThemes(Story story, List<Theme> themes) {
        for (Theme theme : themes) {
            StoryTheme storyTheme = converter.toStoryTheme(story, theme);
            storyThemeRepository.save(storyTheme);
            story.getStoryThemes().add(storyTheme);
        }
    }

    private Background resolveBackground(ConversationRequestDto.ConversationStartRequestDto request) {
        return backgroundRepository.findByName(request.getBackgroundName())
                .orElseGet(() -> backgroundRepository.save(
                        Background.builder()
                                .name(request.getBackgroundName())
                                .build()
                ));
    }

    private void applyBackground(Story story, Background background) {
        StoryBackground storyBackground = converter.toStoryBackground(story, background);
        storyBackgroundRepository.save(storyBackground);
        story.getStoryBackgrounds().add(storyBackground);
    }

    private StoryCharacter createCharacter(Story story, ConversationRequestDto.ConversationStartRequestDto request) {
        return storyCharacterRepository.save(
                converter.toStoryCharacter(story, request)
        );
    }

    private CharacterAppearance createAppearance(StoryCharacter character, ConversationRequestDto.ConversationStartRequestDto request) {
        return characterAppearanceRepository.save(
                converter.toCharacterAppearance(character, request)
        );
    }

    private void applyCharacter(Story story, StoryCharacter character, CharacterAppearance appearance) {
        character.setAppearance(appearance);
        story.setCharacter(character);
    }

    private ConversationSession createSession(Story story, User user) {
        return conversationSessionRepository.save(
                converter.toConversationSession(story, user)
        );
    }

    private String generateStartText(
            Background background,
            StoryCharacter character,
            CharacterAppearance appearance,
            ConversationRequestDto.ConversationStartRequestDto request
    ) {

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

        String promptJson = llmClient.buildPrompt("gui_story_start.json", variable);
        String response = llmClient.callChatGpt(promptJson);

        return llmClient.extractFieldValue(response, "startText");
    }

    private void initializeSessionState(ConversationSession session, String startText) {

        // full story 저장
        session.setFullStory(startText);

        List<ConversationSession.ConversationStep> stepTypes = List.of(
                ConversationSession.ConversationStep.기,
                ConversationSession.ConversationStep.승,
                ConversationSession.ConversationStep.전,
                ConversationSession.ConversationStep.결
        );

        List<SessionStep> steps = stepTypes.stream()
                .map(type -> SessionStep.builder()
                        .stepType(type)
                        .status(SessionStep.Status.NONE)
                        .session(session)
                        .build()
                )
                .toList();

        // SlotDefinition 전체 조회 후 stepType 기준 그룹핑
        List<SlotDefinition> definitions = slotDefinitionRepository.findAll();

        Map<ConversationSession.ConversationStep, List<SlotDefinition>> grouped =
                definitions.stream()
                        .collect(Collectors.groupingBy(SlotDefinition::getStepType));

        // Step별 Slot 생성 (template preload)
        for (SessionStep step : steps) {

            List<SlotDefinition> defs =
                    grouped.getOrDefault(step.getStepType(), List.of());

            List<StepSlot> slots = defs.stream()
                    .map(def -> StepSlot.builder()
                            .step(step)
                            .slotDefinition(def)
                            .isFilled(false)
                            .value(null)
                            .source(null)
                            .build()
                    )
                    .toList();

            step.setSlots(slots);
        }

        // session 연결
        session.setSteps(steps);
    }

}