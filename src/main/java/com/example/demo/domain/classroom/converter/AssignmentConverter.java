package com.example.demo.domain.classroom.converter;

import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AssignmentConverter {

    // ─────────────────────────────────────────────────────
    // 과제 제출 현황 조회 응답 (선생님)
    // ─────────────────────────────────────────────────────

    // approvedStudents: 이름순으로 정렬된 가입 승인 학생 (학급 상세 조회의 번호와 같도록 1부터 순번 부여)
    public ClassroomResponseDto.AssignmentSubmissionResponse toSubmissionResponse(
            Assignment assignment, List<Student> approvedStudents, Set<Long> submittedUserIds) {
        List<ClassroomResponseDto.SubmissionStudentItem> items = new ArrayList<>();

        for (int i = 0; i < approvedStudents.size(); i++) {
            User student = approvedStudents.get(i).getStudent();
            items.add(ClassroomResponseDto.SubmissionStudentItem.builder()
                    .studentNo(i + 1)
                    .studentId(student.getId())
                    .studentName(student.getNickname())
                    .submitted(submittedUserIds.contains(student.getId()))
                    .build());
        }

        int submittedCount = (int) items.stream()
                .filter(ClassroomResponseDto.SubmissionStudentItem::isSubmitted)
                .count();

        return ClassroomResponseDto.AssignmentSubmissionResponse.builder()
                .assignmentId(assignment.getId())
                .title(assignment.getTitle())
                .submittedCount(submittedCount)
                .notSubmittedCount(items.size() - submittedCount)
                .students(items)
                .build();
    }

    // ─────────────────────────────────────────────────────
    // 과제 목록 조회 응답
    // ─────────────────────────────────────────────────────

    public ClassroomResponseDto.TeacherAssignmentResponse toTeacherAssignmentResponse(
            Assignment assignment, int submittedStudent, int totalStudent) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), assignment.getDueAt().toLocalDate());
        return ClassroomResponseDto.TeacherAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .title(assignment.getTitle())
                .dDay(dDay)
                .dueAt(assignment.getDueAt())
                .submittedCount(submittedStudent)
                .notSubmittedCount(totalStudent - submittedStudent)
                .build();
    }

    public ClassroomResponseDto.StudentAssignmentResponse toAssignmentResponse(Assignment assignment, boolean isSubmitted) {
        long dDay = ChronoUnit.DAYS.between(LocalDate.now(), assignment.getDueAt().toLocalDate());
        return ClassroomResponseDto.StudentAssignmentResponse.builder()
                .assignmentId(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dDay(dDay)
                .dueAt(assignment.getDueAt())
                .submitted(isSubmitted)
                .build();
    }
}
