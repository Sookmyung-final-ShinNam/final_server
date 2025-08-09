package com.example.demo.domain.character.entity;

import com.example.demo.domain.story.entity.Story;
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
@Table(name = "character_entity")
public class StoryCharacter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long id;

    // 캐릭터 이름
    @Column(nullable = false, length = 100)
    private String name;

    // 성별 (기본값 FEMALE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.FEMALE;

    // 캐릭터 나이
    @Column(nullable = false)
    private int age;

    // 캐릭터 상태 (기본값 IN_PROGRESS)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterStatus status = CharacterStatus.IN_PROGRESS;

    // 캐릭터 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 캐릭터 성격
    @Column(columnDefinition = "TEXT")
    private String personality;

    // 캐릭터 삭제 시 외형 정보도 삭제
    @OneToOne(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CharacterAppearance appearance;

    // 캐릭터 삭제 시 즐겨찾기 한 캐릭터도 삭제
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserCharacterFavorite> userFavorites = new HashSet<>();

    // 캐릭터 삭제 시 스토리도 삭제
    @OneToOne
    @JoinColumn(name = "story_id", nullable = false, unique = true)
    private Story story;

    public enum Gender {
        FEMALE, // 여성
        MALE    // 남성
    }

    public enum CharacterStatus {
        IN_PROGRESS, // 진행 중
        COMPLETED    // 완료됨
    }
}