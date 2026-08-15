package com.example.demo.domain.classroom.web.dto;

import com.example.demo.domain.classroom.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ClassroomResponseDto {

    // API 1: 학급 생성 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateClassroomResponse {
        private String code;
    }

    // API 2: 선생님 학급 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherClassroomListResponse {
        private String nickname;
        private List<TeacherClassroomItem> classrooms;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherClassroomItem {
        private Long classroomId;
        private String name;
        private int studentCount;
        private String code;
    }

    // API 8: 학급 상세 - 선생님 (PENDING 학생 포함)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherClassroomDetailResponse {
        private String name;
        private int points;
        private LocalDate createdAt;
        private List<TeacherStudentItem> students;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherStudentItem {
        private int number;
        private Long studentId;
        private String studentName;
        private Student.JoinStatus joinStatus;
    }

    // API 8: 학급 상세 - 학생 (APPROVED만)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentClassroomDetailResponse {
        private String name;
        private int points;
        private LocalDate createdAt;
        private List<StudentItem> students;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentItem {
        private int number;
        private Long studentId;
        private String studentName;
    }

    // API 9: 학생 학급 목록 (가입/대기 포함)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentClassroomListResponse {
        private String nickname;
        private List<StudentClassroomItem> classrooms;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentClassroomItem {
        private Long classroomId;
        private String name;
        private int studentCount;
        private String code;
        private Student.JoinStatus joinStatus;
    }

    // API 5: 선생님 전체 과제 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherAssignmentResponse {
        private Long assignmentId;
        private String title;
        private long dDay;
        private LocalDateTime dueAt;
        private long submittedCount;
        private long notSubmittedCount;
    }

    // API 11: 학급 과제 목록 (공통)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentResponse {
        private Long assignmentId;
        private String title;
        private String description;
        private long dDay;
        private LocalDateTime dueAt;
    }
}
