package com.example.demo.domain.character.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "character_appearance_entity")
@Getter
@Setter
@NoArgsConstructor
public class CharacterAppearance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appearance_id")
    private Long id;

    // 눈 색깔
    @Column(name = "eye_color", length = 50)
    private String eyeColor;

    // 머리 색깔
    @Column(name = "hair_color", length = 50)
    private String hairColor;

    // 머리 스타일
    @Column(name = "hair_style", length = 100)
    private String hairStyle;

    // 캐릭터 외형 정보는 캐릭터와 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false, unique = true)
    private StoryCharacter character;
}