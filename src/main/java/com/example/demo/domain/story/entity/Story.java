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

    // 스토리 비디오 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false)
    private StoryPage.VideoStatus videoStatus = StoryPage.VideoStatus.NONE;

    // 이미지용 유튜브 링크
    @Column(name = "image_youtube_link", length = 500)
    private String imageYoutubeLink;

    // 동영상용 유튜브 링크
    @Column(name = "video_youtube_link", length = 500)
    private String videoYoutubeLink;

    // 대시보드에 적용 여부
    @Builder.Default
    @Column(name = "dashboard_applied", nullable = false)
    private boolean dashboardApplied = false;

    // 스토리를 생성한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
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

    // 스토리 삭제 시 캐릭터도 삭제
    @OneToOne(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private StoryCharacter character;

    public enum StoryStatus {
        IN_PROGRESS, // 초기
        MAKING,      // 스토리 제작 중
        TEXT_COMPLETED,  // 텍스트 생성 완료 - 스토리 정제
        TEXT_FAILED,     // 텍스트 생성 실패
        IMAGE_COMPLETED, // 이미지 생성 완료 - 캐릭터 + 페이지
        IMAGE_FAILED,    // 이미지 생성 실패
        VIDEO_COMPLETED, // 동영상 생성 완료
        VIDEO_FAILED     // 동영상 생성 실패
    }

    // 상태 변경 메서드
    public void markVideoAsMaking() {
        if (this.videoStatus == StoryPage.VideoStatus.NONE) {
            this.videoStatus = StoryPage.VideoStatus.MAKING;
        }
    }
}