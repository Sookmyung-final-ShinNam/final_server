package com.example.demo.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "story_page_entity")
public class StoryPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Long id;

    // 페이지 번호
    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    // 페이지 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 페이지 내용 - 영어 버전
    @Column(name = "content_en", columnDefinition = "TEXT")
    private String contentEn;

    // 페이지 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 스토리 페이지는 반드시 하나의 스토리에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
}