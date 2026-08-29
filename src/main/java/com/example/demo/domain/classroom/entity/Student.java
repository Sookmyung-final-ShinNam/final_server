package com.example.demo.domain.classroom.entity;

import com.example.demo.domain.user.entity.User;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "classroom_student_entity",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"classroom_id", "student_id"})}
)
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinStatus status = JoinStatus.PENDING;

    public enum JoinStatus {
        PENDING,   // 가입 요청 중
        APPROVED   // 승인 완료
    }

    @Builder
    private Student(Classroom classroom, User student) {
        this.classroom = classroom;
        this.student = student;
        this.status = JoinStatus.PENDING;
    }

    public void approve() {
        this.status = JoinStatus.APPROVED;
    }
}