package com.example.demo.domain.story.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "background_entity")
@Getter
@Setter
@NoArgsConstructor
public class Background {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "background_id")
    private Long id;

    // 배경 이름
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}