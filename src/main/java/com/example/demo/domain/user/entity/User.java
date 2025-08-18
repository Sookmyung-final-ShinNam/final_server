package com.example.demo.domain.user.entity;

import com.example.demo.domain.character.entity.UserCharacterFavorite;
import com.example.demo.domain.conversation.entity.ConversationSession;
import com.example.demo.domain.story.entity.UserStoryFavorite;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_entity")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 사용자 이메일
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // 사용자 닉네임
    @Column(nullable = false, length = 50)
    private String nickname;

    // 프로필 이미지 URL
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // 현재 보유 포인트 (기본값 500)
    @Column(name = "current_points", nullable = false)
    private int currentPoints = 500;

    // 사용자 등급 (기본값 BASIC)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGrade grade = UserGrade.BASIC;

    // 사용자 상태 (기본값 ACTIVE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    // 삭제 일시 (24시간 이후 삭제 실행, 그 안에 사용자 상태 변화 시 null 로 변환)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 사용자 삭제 시 토큰도 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Token> tokens = new HashSet<>();

    // 사용자 삭제 시 대화 세션도 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConversationSession> sessions = new HashSet<>();

    // 사용자 삭제 시 캐릭터 즐겨찾기도 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserCharacterFavorite> favorites = new HashSet<>();

    // 사용자 삭제 시 스토리 즐겨찾기도 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserStoryFavorite> storyFavorites = new HashSet<>();

    // 약관 1 : 스토리와 캐릭터는 운영자만 삭제 가능합니다. 삭제를 원할 시 별도 신청/문의가 필요합니다.
    //         추후 공유 기능 개발 예정이고 다른 사용자들이 캐릭터와 유대감을 쌓을 수 있으므로 중대한 이유가 아닌 이상 삭제 불가합니다.
    // 약관 2 : 사용자 대화 기록과 즐겨찾기 여부는 탈퇴시 자동으로 삭제됩니다.

    public enum UserGrade {
        BASIC,    // 기본 사용자
        ADMIN     // 관리자
    }

    public enum UserStatus {
        ACTIVE,   // 로그인
        INACTIVE, // 로그아웃
        DELETED   // 회원 탈퇴
    }

    // 사용자 상태를 활성화로 변경
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.deletedAt = null; // 활성화 시 삭제 일시 초기화
    }

}