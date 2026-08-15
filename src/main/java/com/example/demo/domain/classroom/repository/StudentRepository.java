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

    List<Student> findAllByClassroom(Classroom classroom);

    @Query("""
        SELECT s FROM Student s
        WHERE s.classroom = :classroom
        AND s.status = 'APPROVED'
        ORDER BY s.student.nickname ASC
    """)
    List<Student> findApprovedByClassroomOrderByName(@Param("classroom") Classroom classroom);

    @Query("""
        SELECT s FROM Student s
        WHERE s.classroom = :classroom
        ORDER BY s.student.nickname ASC
    """)
    List<Student> findAllByClassroomOrderByName(@Param("classroom") Classroom classroom);

    long countByClassroomAndStatus(Classroom classroom, Student.JoinStatus status);
}
