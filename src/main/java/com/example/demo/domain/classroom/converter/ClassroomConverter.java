package com.example.demo.domain.classroom.converter;

import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClassroomConverter {

    public ClassroomResponseDto.TeacherClassroomListResponse toTeacherListResponse(User teacher, List<Classroom> classrooms) {
        List<ClassroomResponseDto.TeacherClassroomItem> items = classrooms.stream()
                .map(c -> ClassroomResponseDto.TeacherClassroomItem.builder()
                        .classroomId(c.getId())
                        .name(c.getName())
                        .studentCount((int) c.getStudents().stream()
                                .filter(s -> s.getStatus() == Student.JoinStatus.APPROVED)
                                .count())
                        .code(c.getCode())
                        .build())
                .toList();

        return ClassroomResponseDto.TeacherClassroomListResponse.builder()
                .nickname(teacher.getNickname())
                .classrooms(items)
                .build();
    }

    public ClassroomResponseDto.TeacherClassroomDetailResponse toTeacherDetailResponse(Classroom classroom, List<Student> students) {
        List<ClassroomResponseDto.TeacherStudentItem> items = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            items.add(ClassroomResponseDto.TeacherStudentItem.builder()
                    .number(i + 1)
                    .studentId(s.getStudent().getId())
                    .studentName(s.getStudent().getNickname())
                    .joinStatus(s.getStatus())
                    .build());
        }

        return ClassroomResponseDto.TeacherClassroomDetailResponse.builder()
                .name(classroom.getName())
                .points(classroom.getPoints())
                .createdAt(classroom.getCreatedAt().toLocalDate())
                .students(items)
                .build();
    }

    public ClassroomResponseDto.StudentClassroomDetailResponse toStudentDetailResponse(Classroom classroom, List<Student> approvedStudents) {
        List<ClassroomResponseDto.StudentItem> items = new ArrayList<>();
        for (int i = 0; i < approvedStudents.size(); i++) {
            Student s = approvedStudents.get(i);
            items.add(ClassroomResponseDto.StudentItem.builder()
                    .number(i + 1)
                    .studentId(s.getStudent().getId())
                    .studentName(s.getStudent().getNickname())
                    .build());
        }

        return ClassroomResponseDto.StudentClassroomDetailResponse.builder()
                .name(classroom.getName())
                .points(classroom.getPoints())
                .createdAt(classroom.getCreatedAt().toLocalDate())
                .students(items)
                .build();
    }

    public ClassroomResponseDto.StudentClassroomListResponse toStudentListResponse(User student, List<Classroom> classrooms, List<Student> myStudentRecords) {
        List<ClassroomResponseDto.StudentClassroomItem> items = classrooms.stream()
                .map(c -> {
                    Student.JoinStatus status = myStudentRecords.stream()
                            .filter(s -> s.getClassroom().getId().equals(c.getId()))
                            .findFirst()
                            .map(Student::getStatus)
                            .orElse(Student.JoinStatus.PENDING);

                    return ClassroomResponseDto.StudentClassroomItem.builder()
                            .classroomId(c.getId())
                            .name(c.getName())
                            .studentCount((int) c.getStudents().stream()
                                    .filter(s -> s.getStatus() == Student.JoinStatus.APPROVED)
                                    .count())
                            .code(c.getCode())
                            .joinStatus(status)
                            .build();
                })
                .toList();

        return ClassroomResponseDto.StudentClassroomListResponse.builder()
                .nickname(student.getNickname())
                .classrooms(items)
                .build();
    }

    public ClassroomResponseDto.AssignmentResponse toAssignmentResponse(Assignment assignment) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), assignment.getDueAt().toLocalDate());
        return ClassroomResponseDto.AssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dDay(dDay)
                .dueAt(assignment.getDueAt())
                .build();
    }

    public ClassroomResponseDto.TeacherAssignmentResponse toTeacherAssignmentResponse(
            Assignment assignment, long submittedCount, long totalApproved) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), assignment.getDueAt().toLocalDate());
        return ClassroomResponseDto.TeacherAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .title(assignment.getTitle())
                .dDay(dDay)
                .dueAt(assignment.getDueAt())
                .submittedCount(submittedCount)
                .notSubmittedCount(totalApproved - submittedCount)
                .build();
    }
}
