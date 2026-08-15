package com.example.demo.domain.classroom.entity;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(nullable = false, length = 30)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "common_prompt", columnDefinition = "TEXT", nullable = false)
    private String commonPrompt;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @OneToMany(mappedBy = "assignment")
    private Set<Story> stories = new HashSet<>();

    @Builder
    private Assignment(String title, String description, String commonPrompt, LocalDateTime dueAt, Classroom classroom) {
        this.title = title;
        this.description = description;
        this.commonPrompt = commonPrompt;
        this.dueAt = dueAt;
        this.classroom = classroom;
    }
}

