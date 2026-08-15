package com.example.demo.domain.classroom.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentQueryServiceImpl implements AssignmentQueryService {

    private final AssignmentRepository assignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final StoryRepository storyRepository;
    private final ClassroomConverter classroomConverter;

    @Override
    public List<ClassroomResponseDto.TeacherAssignmentResponse> getTeacherAssignments(User teacher) {
        List<Assignment> assignments = assignmentRepository.findAllByTeacherId(teacher.getId());

        return assignments.stream()
                .map(a -> {
                    long approvedCount = studentRepository.countByClassroomAndStatus(
                            a.getClassroom(), Student.JoinStatus.APPROVED);
                    long submittedCount = storyRepository.findAllByAssignment(a).stream()
                            .filter(s -> s.getStoryStatus() != Story.StoryStatus.IN_PROGRESS)
                            .count();
                    return classroomConverter.toTeacherAssignmentResponse(a, submittedCount, approvedCount);
                })
                .toList();
    }

    @Override
    public List<ClassroomResponseDto.AssignmentResponse> getClassroomAssignments(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        boolean isTeacher = classroom.getTeacher().getId().equals(user.getId());
        if (!isTeacher) {
            Student myRecord = studentRepository.findByClassroomAndStudent(classroom, user)
                    .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));
            if (myRecord.getStatus() == Student.JoinStatus.PENDING) {
                throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
            }
        }

        List<Assignment> assignments = assignmentRepository.findAllByClassroomSorted(classroom);
        return assignments.stream()
                .map(classroomConverter::toAssignmentResponse)
                .toList();
    }
}
