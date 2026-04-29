package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findByStorySessions_Id(Long sessionId);

    // 특정 사용자가 작성한 스토리 조회
    List<Story> findByUser(User user);

    // DB 반영 이후의 최신 상태 직접 조회
    @Query("select s.storyStatus from Story s where s.id = :storyId")
    Story.StoryStatus findStoryStatusById(@Param("storyId") Long storyId);

    // 관리자용 -  링크가 비어 있는 동화 조회 : 이미지 숏츠 링크가 없거나, 영상 완료인데 영상 숏츠 링크가 없는 경우
    @Query("""
        SELECT s FROM Story s
        WHERE 
            (s.storyStatus = 'IMAGE_COMPLETED' AND (s.imageYoutubeLink IS NULL OR s.imageYoutubeLink = ''))
         OR
            (s.storyStatus = 'IMAGE_COMPLETED' AND s.videoStatus = 'COMPLETED' 
             AND (s.videoYoutubeLink IS NULL OR s.videoYoutubeLink = ''))
    """)
    List<Story> findIncompleteStoriesForAdmin();

    // 관리자용 - 스토리 재생성 배치 실패 동화 조회 (배치 횟수 3회 이상이고, 완성 상태가 아닌 동화)
    @Query("""
        SELECT s FROM Story s
        WHERE s.retryCount >= 3
            AND s.storyStatus NOT IN :complete
    """)
    List<Story> findFailedRetryStoriesForAdmin(
            @Param("complete") List<Story.StoryStatus> completeStatus
    );
}