package com.example.demo.domain.user.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_entity")
@Getter
@Setter
@NoArgsConstructor
public class Token extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토큰을 소유한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 액세스 토큰
    @Column(name = "access_token", nullable = false, unique = true, length = 500)
    private String accessToken;

    // 리프레시 토큰
    @Column(name = "refresh_token", nullable = false, unique = true, length = 500)
    private String refreshToken;
}