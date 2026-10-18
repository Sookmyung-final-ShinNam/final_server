package com.example.demo.domain.classroom.repository;

import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.classroom.entity.Student;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByClassroomAndStudent(Classroom classroom, User student);

    Optional<Student> findByClassroomAndStudent(Classroom classroom, User student);


    Optional<Student> findByClassroomIdAndStudentId(Long classroomId, Long studentId);

    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.classroom
        WHERE s.student = :student
        ORDER BY s.classroom.createdAt DESC
    """)
    List<Student> findAllByStudentWithClassroom(@Param("student") User student);

    /**
     * 모든 학생 조회
     *
     * 1. PENDING (이름순)
     * 2. APPROVED (이름순)
     * */
    @Query("""
        SELECT s FROM Student s
        WHERE s.classroom = :classroom
        ORDER BY CASE WHEN s.status = 'PENDING' THEN 0 ELSE 1 END ASC,
             s.student.nickname ASC
    """)
    List<Student> findAllByClassroomOrderByStatusAndName(@Param("classroom") Classroom classroom);

    /**
     * 가입 승인된 모든 학생 조회 (이름순)
     * */
    @Query("""
        SELECT s FROM Student s
        WHERE s.classroom = :classroom
        AND s.status = 'APPROVED'
        ORDER BY s.student.nickname ASC
    """)
    List<Student> findApprovedByClassroomOrderByName(@Param("classroom") Classroom classroom);

    long countByClassroomAndStatus(Classroom classroom, Student.JoinStatus status);
}
