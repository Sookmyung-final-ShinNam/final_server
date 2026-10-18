package com.example.demo.domain.classroom.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.converter.CharacterConverter;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.converter.AssignmentConverter;
import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.AssignmentRepository;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.conversation.repository.ConversationSessionRepository;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentQueryServiceImpl implements AssignmentQueryService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ConversationSessionRepository sessionRepository;
    private final StoryRepository storyRepository;
    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;

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

    // 과제 제출 현황 조회 (선생님): 가입 승인된 학생별 제출 여부
    @Override
    public ClassroomResponseDto.AssignmentSubmissionResponse getAssignmentSubmissions(
            User user, Long classroomId, Long assignmentId) {

        // 유저 역할 체크
        if (!user.getGrade().isTeacher()) {
            throw new CustomException(ErrorStatus.USER_ROLE_NOT_ALLOWED);
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        // 본인 학급인지 확인
        if (!classroom.getTeacher().getId().equals(user.getId())) {
            throw new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED);
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomException(ErrorStatus.ASSIGNMENT_NOT_FOUND));

        // 해당 학급의 과제인지 확인
        if (!assignment.getClassroom().getId().equals(classroomId)) {
            throw new CustomException(ErrorStatus.ASSIGNMENT_ACCESS_DENIED);
        }

        // 가입 승인된 학생 (이름순) + 과제 제출(세션 완료)한 학생 id
        List<Student> approvedStudents = studentRepository.findApprovedByClassroomOrderByName(classroom);
        Set<Long> submittedUserIds = new HashSet<>(sessionRepository.findSubmittedUserIdsByAssignmentId(
                assignmentId, ConversationSession.SessionState.COMPLETED));

        return assignmentConverter.toSubmissionResponse(assignment, approvedStudents, submittedUserIds);
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

        // 4. 내 관심 캐릭터 id (일반 보관함과 같은 관심 캐릭터 목록. 친구가 만든 캐릭터도 포함될 수 있음)
        Set<Long> favoriteIds = userCharacterFavoriteRepository.findByUser(user).stream()
                .map(fav -> fav.getCharacter().getId())
                .collect(Collectors.toSet());

        // 5. DTO 변환 (만든 사용자 닉네임 포함)
        return characterConverter.toCharacterListResponse(sorted, favoriteIds, true);
    }
}
