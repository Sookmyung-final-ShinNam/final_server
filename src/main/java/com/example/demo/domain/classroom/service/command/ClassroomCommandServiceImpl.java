package com.example.demo.domain.classroom.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomCommandServiceImpl implements ClassroomCommandService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────
    // 선생님 기능
    // ─────────────────────────────────────────────────────

    // 학급 생성
    @Override
    public ClassroomResponseDto.CreateClassroomResponse createClassroom(User teacher, String name) {

        // 유저 역할 체크
        validateRole(teacher, User.UserGrade.TEACHER);

        // 고유 학급 코드 생성 (8자리 대문자)
        String code = generateUniqueCode();

        Classroom classroom = Classroom.builder()
                .name(name)
                .code(code)
                .teacher(teacher)
                .build();
        classroomRepository.save(classroom);

        return ClassroomResponseDto.CreateClassroomResponse.builder()
                .code(code)
                .build();
    }

    // 8자리 대문자 생성
    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (classroomRepository.existsByCode(code));
        return code;
    }

    // 가입 요청 학생 승인 / 거절
    @Override
    public void approveStudent(User teacher, Long classroomId, Long studentId) {

        // 유저 역할 체크
        validateRole(teacher, User.UserGrade.TEACHER);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED);
        }

        Student student = studentRepository.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDENT_NOT_FOUND));

        student.approve();
    }

    // 학급 도토리 충전
    @Override
    public void chargeAcorn(User teacher, Long classroomId) {

        // 유저 역할 체크
        validateRole(teacher, User.UserGrade.TEACHER);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED);
        }

        classroom.chargePoints(); // 20 초과 시 내부에서 예외 발생
    }

    // ─────────────────────────────────────────────────────
    // 학생 기능
    // ─────────────────────────────────────────────────────

    // 학급 가입
    @Override
    public void joinClassroom(User student, String code) {

        // 유저 역할 체크
        validateRole(student, User.UserGrade.BASIC);

        Classroom classroom = classroomRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_INVALID_CODE));

        if (studentRepository.existsByClassroomAndStudent(classroom, student)) {
            throw new CustomException(ErrorStatus.CLASSROOM_ALREADY_JOINED);
        }

        Student newStudent = Student.builder()
                .classroom(classroom)
                .student(student)
                .build();
        studentRepository.save(newStudent);
    }

    // 사용자 역할 확인
    private void validateRole(User user, User.UserGrade role) {
        if (user.getGrade() != role) {
            throw new CustomException(ErrorStatus.USER_ROLE_NOT_ALLOWED);
        }
    }
}
