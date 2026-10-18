package com.example.demo.domain.user.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "email_verification_entity")
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 이메일
    private String email;

    // 이메일 인증 코드
    private String code;

    // 이메일 인증 시각 (null이면 미인증)
    private LocalDateTime verifiedAt;

    // 이메일을 소유한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 생성자
    @Builder
    private EmailVerification(String email, String code, User user) {
        this.email = email;
        this.code = code;
        this.user = user;
    }

    // 인증코드 만료 여부 > TTL(5분)
    public boolean isExpired() {
        return getCreatedAt().isBefore(BaseEntity.now().minusMinutes(5));
    }

    // 인증코드 일치 확인
    public boolean isEqual(String emailCode) {
        return this.code.equals(emailCode);
    }

    // 이메일 인증 시각 기록
    public void verify() {
        this.verifiedAt = BaseEntity.now();
    }

    // 이메일 인증 여부
    public boolean isVerified() {
        return this.verifiedAt != null;
    }

    // 이메일 인증 유효 여부 > TTL(30분)
    public boolean isValid() {
        return isVerified() && this.verifiedAt.isAfter(BaseEntity.now().minusMinutes(30));
    }
}
