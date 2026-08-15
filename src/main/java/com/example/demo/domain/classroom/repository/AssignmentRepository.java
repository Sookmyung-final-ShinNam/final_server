package com.example.demo.domain.classroom.repository;

import com.example.demo.domain.classroom.entity.Assignment;
import com.example.demo.domain.classroom.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByClassroomOrderByCreatedAtAsc(Classroom classroom);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.classroom.teacher.id = :teacherId
        ORDER BY a.createdAt ASC
    """)
    List<Assignment> findAllByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.classroom = :classroom
        ORDER BY a.createdAt ASC
    """)
    List<Assignment> findAllByClassroomSorted(@Param("classroom") Classroom classroom);
}
