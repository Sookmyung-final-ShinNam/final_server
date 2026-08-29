package com.example.demo.domain.classroom.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.converter.CharacterConverter;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.converter.AssignmentConverter;
import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.AssignmentRepository;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentQueryServiceImpl implements AssignmentQueryService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ConversationSessionRepository sessionRepository;
    private final StoryRepository storyRepository;

    private final AssignmentConverter assignmentConverter;
    private final CharacterConverter characterConverter;

    @Override
    public Object getClassroomAssignments(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        boolean isTeacher = user.getGrade().isTeacher() && classroom.getTeacher().getId().equals(user.getId());

        if (isTeacher) {
            // 선생님: 학급의 전체 과제 조회 (등록순)
            List<Assignment> assignments = assignmentRepository.findAllByClassroomSorted(classroom);
            int totalStudent = classroom.countApprovedStudents();

            return assignments.stream()
                    .map(a -> {
                        int submittedStudent = sessionRepository.countByStory_AssignmentIdAndState(
                                a.getId(), ConversationSession.SessionState.COMPLETED);
                        return assignmentConverter.toTeacherAssignmentResponse(a, submittedStudent, totalStudent);
                    })
                    .toList();
        }

        // 학생: 본인이 가입된 학급인지 확인
        Student myEnrollment = studentRepository.findByClassroomAndStudent(classroom, user)
                    .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));

        if (myEnrollment.getStatus() == Student.JoinStatus.PENDING) {
                throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
        }

        List<Assignment> assignments = assignmentRepository.findAllByClassroomSorted(classroom);
        return assignments.stream()
                .map(a -> {
                    boolean isSubmitted = sessionRepository.existsByStory_AssignmentIdAndUserIdAndState(
                            a.getId(), user.getId(), ConversationSession.SessionState.COMPLETED);
                    return assignmentConverter.toAssignmentResponse(a, isSubmitted);
                })
                .toList();
    }

    @Override
    public CompletedCharacterResponse.CharacterListResponse getClassroomStories(User user, Long classroomId,  Long assignmentId) {

        // 1. 학급 조회 + 접근 권한 검증
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        boolean isTeacher = user.getGrade().isTeacher() && classroom.getTeacher().getId().equals(user.getId());
        if (!isTeacher) {
            Student myRecord = studentRepository.findByClassroomAndStudent(classroom, user)
                    .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));
            if (myRecord.getStatus() == Student.JoinStatus.PENDING) {
                throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
            }
        }

        // 2. 동화 조회 (완료된 것만) - assignmentId 유무에 따라 분기
        List<Story> stories;
        if (assignmentId != null) {
            stories = storyRepository.findByAssignmentIdAndStoryStatus(
                    assignmentId, Story.StoryStatus.IMAGE_COMPLETED);
        } else {
            stories = storyRepository.findByClassroomIdAndStoryStatus(
                    classroomId, Story.StoryStatus.IMAGE_COMPLETED);
        }

        // 3. 캐릭터 정렬 (관심 캐릭터 개념 없음 → 항상 최신순만)
        List<StoryCharacter> characters = stories.stream()
                .map(Story::getCharacter)
                .toList();

        List<StoryCharacter> sorted = characters.stream()
                .sorted(Comparator.comparing(StoryCharacter::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        // 4. DTO 변환 (관심 캐릭터는 항상 빈 Set → isFavorite 항상 false)
        return characterConverter.toCharacterListResponse(sorted, Collections.emptySet());
    }
}
