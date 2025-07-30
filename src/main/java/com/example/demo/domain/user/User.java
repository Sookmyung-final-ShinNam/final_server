package com.example.demo.domain.user;

import com.example.demo.domain.character.UserCharacterFavorite;
import com.example.demo.domain.conversation.ConversationSession;
import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_entity")
@Getter
@Setter
@NoArgsConstructor
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


    public enum UserStatus {
        ACTIVE,   // 로그인
        INACTIVE, // 로그아웃
        DELETED   // 회원 탈퇴
    }
}