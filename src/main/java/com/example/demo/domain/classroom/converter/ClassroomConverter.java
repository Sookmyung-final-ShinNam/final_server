package com.example.demo.domain.classroom.converter;

import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClassroomConverter {

    // ─────────────────────────────────────────────────────
    // 학급 목록 조회 응답
    // ─────────────────────────────────────────────────────

    public ClassroomResponseDto.TeacherClassroomListResponse toTeacherListResponse(User teacher, List<Classroom> classrooms) {
        List<ClassroomResponseDto.TeacherClassroomItem> items = classrooms.stream()
                .map(c -> ClassroomResponseDto.TeacherClassroomItem.builder()
                        .classroomId(c.getId())
                        .name(c.getName())
                        .studentCount(c.countApprovedStudents())
                        .code(c.getCode())
                        .build())
                .toList();

        return ClassroomResponseDto.TeacherClassroomListResponse.builder()
                .nickname(teacher.getNickname())
                .classrooms(items)
                .build();
    }

    public ClassroomResponseDto.StudentClassroomListResponse toStudentListResponse(User student, List<Student> myEnrollments) {
        List<ClassroomResponseDto.StudentClassroomItem> items = myEnrollments.stream()
                .map(s -> {
                    Classroom c = s.getClassroom();
                    return ClassroomResponseDto.StudentClassroomItem.builder()
                            .classroomId(c.getId())
                            .name(c.getName())
                            .studentCount(c.countApprovedStudents())
                            .code(c.getCode())
                            .joinStatus(s.getStatus())
                            .build();
                })
                .toList();

        return ClassroomResponseDto.StudentClassroomListResponse.builder()
                .nickname(student.getNickname())
                .classrooms(items)
                .build();
    }

    // ─────────────────────────────────────────────────────
    // 학급 상세 조회 응답
    // ─────────────────────────────────────────────────────

    public ClassroomResponseDto.TeacherClassroomDetailResponse toTeacherDetailResponse(Classroom classroom, List<Student> students) {
        List<ClassroomResponseDto.TeacherStudentItem> items = new ArrayList<>();
        int approvedNumber = 0;

        for (Student s : students) {
            int number = 0; // 기본값 0 (PENDING인 학생 번호)

            if (s.getStatus() == Student.JoinStatus.APPROVED) {
                approvedNumber++;
                number = approvedNumber;
            }

            items.add(ClassroomResponseDto.TeacherStudentItem.builder()
                    .studentNo(number)
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
                    .studentNo(i + 1)
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
}
