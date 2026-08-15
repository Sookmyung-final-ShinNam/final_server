package com.example.demo.domain.classroom.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.character.repository.UserCharacterFavoriteRepository;
import com.example.demo.domain.classroom.converter.ClassroomConverter;
import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.classroom.repository.AssignmentRepository;
import com.example.demo.domain.classroom.repository.ClassroomRepository;
import com.example.demo.domain.classroom.repository.StudentRepository;
import com.example.demo.domain.story.repository.StoryRepository;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomQueryServiceImpl implements ClassroomQueryService {

    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final ClassroomConverter classroomConverter;

    // 학급 목록 조회
    @Override
    public Object getClassrooms(User user) {

        // 선생님 소유의 모든 학급 조회
       if (user.getGrade().isTeacher()) {
           List<Classroom> classrooms = classroomRepository.findAllByTeacher(user);
           return classroomConverter.toTeacherListResponse(user, classrooms);
       }

        // 학생 소유의 모든 학급 조회
        List<Student> myEnrollments = studentRepository.findAllByStudentWithClassroom(user);
        return classroomConverter.toStudentListResponse(user, myEnrollments);
    }

    // 학급 상세 조회
    @Override
    public Object getClassroomDetail(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_NOT_FOUND));

        boolean isTeacher = user.getGrade().isTeacher() && classroom.getTeacher().getId().equals(user.getId());

        if (isTeacher) {
            // 선생님: 전체 학생 조회 (PENDING > APPROVED 순, 각 상태 내 이름순)
            List<Student> allStudents = studentRepository.findAllByClassroomOrderByStatusAndName(classroom);
            return classroomConverter.toTeacherDetailResponse(classroom, allStudents);
        }

        // 학생: 본인이 가입된 학급인지 확인
        Student myEnrollment = studentRepository.findByClassroomAndStudent(classroom, user)
                .orElseThrow(() -> new CustomException(ErrorStatus.CLASSROOM_ACCESS_DENIED));

        if (myEnrollment.getStatus() == Student.JoinStatus.PENDING) {
            throw new CustomException(ErrorStatus.CLASSROOM_NOT_APPROVED);
        }

        // 학생: APPROVED 상태 학생 조회 (이름순)
        List<Student> approvedStudents = studentRepository.findApprovedByClassroomOrderByName(classroom);
        return classroomConverter.toStudentDetailResponse(classroom, approvedStudents);
    }
}
