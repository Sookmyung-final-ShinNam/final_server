package com.example.demo.domain.classroom.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.EmailVerification;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.EmailVerificationRepository;
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
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    @Override
    public void sendEmailVerification(User user, String email) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 이미 다른 계정에서 인증된 이메일인지 확인 (본인 제외)
        emailVerificationRepository.findByEmail(email)
                .filter(EmailVerification::isVerified)
                .filter(ev -> !ev.getUser().getId().equals(currentUser.getId()))
                .ifPresent(ev -> {
                    throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_ALREADY_COMPLETED);
                });

        // 기존 인증 내역 삭제 후 재생성
        emailVerificationRepository.deleteAllByUser(currentUser);

        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .user(currentUser)
                .build();
        emailVerificationRepository.save(verification);

        // TODO: mailSender.send(email, "[ShinNam] 학급 생성 이메일 인증", "인증코드: " + code);
    }

    @Override
    public void verifyEmail(User user, String code) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        EmailVerification verification = emailVerificationRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus.EMAIL_VERIFICATION_NOT_FOUND));

        if (verification.isExpired()) {
            throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!verification.isEqual(code)) {
            throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verify();
    }

    @Override
    public ClassroomResponseDto.CreateClassroomResponse createClassroom(User teacher, String name) {
        User currentUser = userRepository.findById(teacher.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        // 이메일 인증 확인 (30분 유효)
        EmailVerification verification = emailVerificationRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus.EMAIL_VERIFICATION_NOT_FOUND));

        if (!verification.isValid()) {
            throw new CustomException(ErrorStatus.EMAIL_VERIFICATION_NOT_COMPLETED);
        }

        // 고유 학급 코드 생성 (8자리 대문자)
        String code = generateUniqueCode();

        Classroom classroom = Classroom.builder()
                .name(name)
                .code(code)
                .teacher(currentUser)
                .build();
        classroomRepository.save(classroom);

        return ClassroomResponseDto.CreateClassroomResponse.builder()
                .code(code)
                .build();
    }

    @Override
    public void approveStudent(User teacher, Long classroomId, Long studentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED);
        }

        Student student = studentRepository.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDENT_NOT_FOUND));

        student.approve();
    }

    @Override
    public void joinClassroom(User student, String code) {
        Classroom classroom = classroomRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_INVALID_CODE));

        User currentUser = userRepository.findById(student.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        if (studentRepository.existsByClassroomAndStudent(classroom, currentUser)) {
            throw new CustomException(ErrorStatus.CLASSROOM_ALREADY_JOINED);
        }

        Student newStudent = Student.builder()
                .classroom(classroom)
                .student(currentUser)
                .build();
        studentRepository.save(newStudent);
    }

    @Override
    public void chargeAcorn(User user) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorStatus.USER_NOT_FOUND));

        currentUser.chargeAcorn(); // 20 초과 시 내부에서 예외 발생
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (classroomRepository.existsByCode(code));
        return code;
    }
}
