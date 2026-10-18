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

    // 미제출 학생 알림 메일을 마지막으로 보낸 시각 (스팸 방지용, 보낸 적 없으면 null)
    @Column(name = "last_reminded_at")
    private LocalDateTime lastRemindedAt;

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

    // 마감 시각이 지났는지
    public boolean isClosed(LocalDateTime now) {
        return dueAt.isBefore(now);
    }

    // 알림 메일을 보낼 수 있는지 (마지막 발송 후 cooldownHours 시간이 지났거나 보낸 적이 없는 경우)
    public boolean canRemind(LocalDateTime now, long cooldownHours) {
        return lastRemindedAt == null || lastRemindedAt.plusHours(cooldownHours).isBefore(now);
    }

    // 알림 메일 발송 시각 기록
    public void markReminded(LocalDateTime now) {
        this.lastRemindedAt = now;
    }
}

