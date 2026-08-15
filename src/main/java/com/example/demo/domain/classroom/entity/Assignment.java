package com.example.demo.domain.classroom.entity;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "classroom_assignment_entity")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 과제 제목
    @Column(nullable = false, length = 30)
    private String title;

    // 과제 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 공통 프롬프트
    @Column(name = "common_prompt", columnDefinition = "TEXT", nullable = false)
    private String commonPrompt;

    // 마감기한
    @Column(nullable = false)
    private LocalDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // 학급(=과제) 사라지면 story는 assignments = id null 처리를 원함
    @OneToMany(mappedBy = "assignment")
    private Set<Story> stories = new HashSet<>();
}

