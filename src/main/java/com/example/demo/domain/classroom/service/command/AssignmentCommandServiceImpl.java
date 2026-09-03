package com.example.demo.domain.classroom.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.CharacterAppearance;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.CharacterAppearanceRepository;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.AssignmentRepository;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.classroom.web.dto.ClassroomRequestDto;
import com.example.demo.domain.conversation.converter.ConversationConverter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.entity.SessionStep;
import com.example.demo.domain.conversation.entity.SlotDefinition;
import com.example.demo.domain.conversation.entity.StepSlot;
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
@Transactional
public class AssignmentCommandServiceImpl implements AssignmentCommandService {

    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
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

    // ─────────────────────────────────────────────────────
    // 선생님 기능
    // ─────────────────────────────────────────────────────

    // 과제 생성
    @Override
    public void createAssignment(User teacher, Long classroomId, ClassroomRequestDto.CreateAssignmentRequest request) {

        // 유저 역할 체크
        if (!teacher.getGrade().isTeacher()) {
            throw new CustomException(ErrorStatus.USER_ROLE_NOT_ALLOWED);
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED);
        }

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .commonPrompt(request.getCommonPrompt())
                .dueAt(request.getDueAt().withSecond(59).withNano(0)) // 59초 설정
                .classroom(classroom)
                .build();
        assignmentRepository.save(assignment);
    }

    // ─────────────────────────────────────────────────────
    // 학생 기능
    // ─────────────────────────────────────────────────────

    // 과제 진행
    @Override
    public ConversationResponseDto.ConversationStartResponseDto startAssignment(
            User student, Long classroomId, Long assignmentId,
            ConversationRequestDto.ConversationStartRequestDto request) {

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorStatus.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getClassroom().getId().equals(classroomId)) {
            throw new CustomException(ErrorStatus.ASSIGNMENT_ACCESS_DENIED);
        }

        User currentUser = userRepository.findById(student.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 승인된 학생인지 확인
        Student studentRecord = studentRepository.findByClassroomAndStudent(classroom, currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED));

        if (studentRecord.getStatus() != Student.JoinStatus.APPROVED) {
            throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
        }

        classroom.usePoints(1);

        // 과제 스토리 생성 (CLASSROOM 타입, assignment 연결)
        Story story = storyRepository.save(converter.toClassroomStory(currentUser, assignment));

        List<Theme> themes = resolveThemes(request);
        applyThemes(story, themes);

        Background background = resolveBackground(request);
        applyBackground(story, background);

        StoryCharacter character = storyCharacterRepository.save(
                converter.toStoryCharacter(story, request));
        CharacterAppearance appearance = characterAppearanceRepository.save(
                converter.toCharacterAppearance(character, request));
        character.setAppearance(appearance);
        story.setCharacter(character);

        ConversationSession session = conversationSessionRepository.save(
                converter.toConversationSession(story, currentUser));

        // 과제 commonPrompt를 포함해 LLM 호출
        String startText = generateStartText(background, character, appearance, request, assignment.getCommonPrompt());

        initializeSessionState(session, startText);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishEvent(new StartConversationEvent(session.getId()));
                    }
                }
        );

        return ConversationResponseDto.ConversationStartResponseDto.builder()
                .sessionId(session.getId())
                .nextStory(startText)
                .currentStep(ConversationSession.ConversationStep.START)
                .build();
    }

    private List<Theme> resolveThemes(ConversationRequestDto.ConversationStartRequestDto request) {
        return request.getThemeNames().stream()
                .map(name -> themeRepository.findByName(name)
                        .orElseGet(() -> themeRepository.save(Theme.builder().name(name).build())))
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
                        Background.builder().name(request.getBackgroundName()).build()));
    }

    private void applyBackground(Story story, Background background) {
        StoryBackground storyBackground = converter.toStoryBackground(story, background);
        storyBackgroundRepository.save(storyBackground);
        story.getStoryBackgrounds().add(storyBackground);
    }

    private String generateStartText(
            Background background, StoryCharacter character, CharacterAppearance appearance,
            ConversationRequestDto.ConversationStartRequestDto request, String commonPrompt) {

        String variable = llmClient.jsonEscape(
                "동화 배경: " + background.getName() +
                ", 동화 테마: " + request.getThemeNames() +
                ", 캐릭터 이름: " + character.getName() +
                ", 캐릭터 나이: " + character.getAge() +
                ", 캐릭터 성별: " + character.getGender() +
                ", 캐릭터 눈 색: " + appearance.getEyeColor() +
                ", 캐릭터 머리 색: " + appearance.getHairColor() +
                ", 캐릭터 머리 스타일: " + appearance.getHairStyle() +
                ", 과제 지침: " + commonPrompt
        );

        String promptJson = llmClient.buildPrompt("gui_story_start.json", variable);
        String response = llmClient.callChatGpt(promptJson);
        return llmClient.extractFieldValue(response, "startText");
    }

    private void initializeSessionState(ConversationSession session, String startText) {
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
                        .build())
                .toList();

        List<SlotDefinition> definitions = slotDefinitionRepository.findAll();
        Map<ConversationSession.ConversationStep, List<SlotDefinition>> grouped =
                definitions.stream().collect(Collectors.groupingBy(SlotDefinition::getStepType));

        for (SessionStep step : steps) {
            List<SlotDefinition> defs = grouped.getOrDefault(step.getStepType(), List.of());
            List<StepSlot> slots = defs.stream()
                    .map(def -> StepSlot.builder()
                            .step(step)
                            .slotDefinition(def)
                            .isFilled(false)
                            .value(null)
                            .source(null)
                            .build())
                    .toList();
            step.setSlots(slots);
        }

        session.setSteps(steps);
    }
}
