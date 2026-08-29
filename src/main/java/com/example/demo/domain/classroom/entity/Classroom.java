package com.example.demo.domain.classroom.entity;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "classroom_entity")
public class Classroom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private int points = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assignment> assignments = new HashSet<>();

    // 생성자
    @Builder
    private Classroom(String name, String code, User teacher) {
        this.name = name;
        this.code = code;
        this.teacher = teacher;
    }

    // 학급 포인트 충전 (5개씩, 최대 20개)
    public void chargePoints() {
        if (this.points + 5 > 20) throw new CustomException(ErrorStatus.CLASSROOM_ACORN_LIMIT_EXCEEDED);
        this.points += 5;
    }

    // 가입 승인된 학생 수 세기
    public int countApprovedStudents() {
        return (int) this.students.stream()
                .filter(s -> s.getStatus() == Student.JoinStatus.APPROVED)
                .count();
    }
}