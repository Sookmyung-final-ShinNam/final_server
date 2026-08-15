package com.example.demo.domain.classroom.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.entity.UserCharacterFavorite;
import com.example.demo.domain.character.repository.StoryCharacterRepository;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.converter.ClassroomConverter;
import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.AssignmentRepository;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomQueryServiceImpl implements ClassroomQueryService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final StoryRepository storyRepository;
    private final StoryCharacterRepository storyCharacterRepository;
    private final UserCharacterFavoriteRepository userCharacterFavoriteRepository;
    private final ClassroomConverter classroomConverter;

    @Override
    public ClassroomResponseDto.TeacherClassroomListResponse getTeacherClassrooms(User teacher) {
        List<Classroom> classrooms = classroomRepository.findAllByTeacher(teacher);
        return classroomConverter.toTeacherListResponse(teacher, classrooms);
    }

    @Override
    public ClassroomResponseDto.StudentClassroomListResponse getStudentClassrooms(User student) {
        // 학생이 가입 요청한 모든 학급 조회
        List<Classroom> classrooms = classroomRepository.findAllByStudent(student);

        // 내 Student 레코드 목록 (joinStatus 포함)
        List<Student> studentRecords = classrooms.stream()
                .map(c -> studentRepository.findByClassroomAndStudent(c, student).orElse(null))
                .filter(s -> s != null)
                .toList();

        return classroomConverter.toStudentListResponse(student, classrooms, studentRecords);
    }

    @Override
    public Object getClassroomDetail(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        boolean isTeacher = classroom.getTeacher().getId().equals(user.getId());

        if (isTeacher) {
            // 선생님: 전체 학생 (PENDING 포함), 이름순
            List<Student> allStudents = studentRepository.findAllByClassroomOrderByName(classroom);
            return classroomConverter.toTeacherDetailResponse(classroom, allStudents);
        }

        // 학생: 본인이 승인된 학급인지 확인
        Student myRecord = studentRepository.findByClassroomAndStudent(classroom, user)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));

        if (myRecord.getStatus() == Student.JoinStatus.PENDING) {
            throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
        }

        // 학생: APPROVED만, 이름순
        List<Student> approvedStudents = studentRepository.findApprovedByClassroomOrderByName(classroom);
        return classroomConverter.toStudentDetailResponse(classroom, approvedStudents);
    }

    @Override
    public CompletedCharacterResponse.CharacterListResponse getClassroomStories(User user, Long classroomId, Integer week) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        // 학급 접근 권한 확인 (선생님 또는 승인된 학생)
        boolean isTeacher = classroom.getTeacher().getId().equals(user.getId());
        if (!isTeacher) {
            Student myRecord = studentRepository.findByClassroomAndStudent(classroom, user)
                    .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));
            if (myRecord.getStatus() == Student.JoinStatus.PENDING) {
                throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
            }
        }

        // N번째 과제 조회 (week = 1부터 시작)
        List<Assignment> assignments = assignmentRepository.findAllByClassroomSorted(classroom);
        if (week == null || week < 1 || week > assignments.size()) {
            throw new CustomException(ErrorStatus.ASSIGNMENT_NOT_FOUND);
        }
        Assignment targetAssignment = assignments.get(week - 1);

        // 해당 과제로 만든 완성된 동화의 캐릭터 조회
        Set<Long> favoriteCharacterIds = userCharacterFavoriteRepository.findAllByUser(user).stream()
                .map(f -> f.getCharacter().getId())
                .collect(Collectors.toSet());

        List<Story> stories = storyRepository.findAllByAssignment(targetAssignment).stream()
                .filter(s -> s.getStoryStatus().isCompletedStory())
                .toList();

        List<CompletedCharacterResponse> characters = stories.stream()
                .filter(s -> s.getCharacter() != null)
                .map(s -> {
                    StoryCharacter c = s.getCharacter();
                    return CompletedCharacterResponse.builder()
                            .characterId(c.getId())
                            .name(c.getName())
                            .gender(c.getGender().name())
                            .imageUrl(c.getImageUrl())
                            .important(favoriteCharacterIds.contains(c.getId()))
                            .createTime(c.getCreatedAt())
                            .build();
                })
                .toList();

        return CompletedCharacterResponse.CharacterListResponse.builder()
                .characters(characters)
                .build();
    }
}
