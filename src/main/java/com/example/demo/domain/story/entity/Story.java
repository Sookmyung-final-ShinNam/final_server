package com.example.demo.domain.story.entity;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "story_entity")
public class Story extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    // 스토리 제목
    @Column(length = 100)
    private String title;

    // 스토리 3줄 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 스토리 상태 (기본값 IN_PROGRESS)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryStatus status = StoryStatus.IN_PROGRESS;

    // 스토리를 생성한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 스토리 삭제 시 스토리 테마도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryTheme> storyThemes = new HashSet<>();

    // 스토리 삭제 시 스토리 배경도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryBackground> storyBackgrounds = new HashSet<>();

    // 스토리 삭제 시 스토리 페이지도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private Set<StoryPage> storyPages = new HashSet<>();

    // 스토리 삭제 시 대화 세션도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConversationSession> storySessions = new HashSet<>();

    // 스토리 삭제 시 스토리 즐겨찾기도 삭제
    @Builder.Default
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserStoryFavorite> userStoryFavorites = new HashSet<>();

    // 스토리 삭제 시 캐릭터도 삭제
    @OneToOne(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private StoryCharacter character;

    public enum StoryStatus {
        IN_PROGRESS, // 진행 중
        MAKING,      // complete 수행 중
        COMPLETED    // 완료됨
    }
}