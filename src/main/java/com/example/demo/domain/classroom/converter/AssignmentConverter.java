package com.example.demo.domain.classroom.converter;

import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class AssignmentConverter {

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
