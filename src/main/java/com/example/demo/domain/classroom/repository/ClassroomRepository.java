package com.example.demo.domain.classroom.repository;

import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findAllByTeacher(User teacher);

    Optional<Classroom> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
        SELECT c FROM Classroom c
        JOIN c.students s
        WHERE s.student = :student
    """)
    List<Classroom> findAllByStudent(@Param("student") User student);
}
