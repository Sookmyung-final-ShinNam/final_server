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

    // 동영상 생성 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false)
    private VideoStatus videoStatus = VideoStatus.NONE;

    public enum VideoStatus {
        NONE,        // 아직 생성 안됨
        MAKING,      // 생성 중
        COMPLETED    // 생성 완료
    }

    // 페이지 동영상 URL
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // 스토리 페이지는 반드시 하나의 스토리에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
}