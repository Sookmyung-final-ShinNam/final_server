package com.example.demo.domain.classroom.repository;

import com.example.demo.domain.classroom.entity.Classroom;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
        SELECT c FROM Classroom c
        WHERE c.teacher = :teacher
        ORDER BY c.createdAt DESC
    """)
    List<Classroom> findAllByTeacher(User teacher);
}
