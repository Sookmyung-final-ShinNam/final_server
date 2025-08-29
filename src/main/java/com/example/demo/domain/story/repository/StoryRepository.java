package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findByStorySessions_Id(Long sessionId);

    @Query("""
    SELECT s FROM Story s
    LEFT JOIN s.userStoryFavorites f WITH f.user = :user
    WHERE s.user = :user
    ORDER BY 
        CASE s.status
            WHEN 'IN_PROGRESS' THEN 0
            WHEN 'MAKING' THEN 1
            WHEN 'COMPLETED' THEN 2
        END ASC,
        CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END DESC,
        s.createdAt DESC
""")
    // 해당 유저가 작성한 스토리를 상태(IN_PROGRESS → MAKING → COMPLETED), 즐겨찾기 여부(즐겨찾기 우선), 생성일 최신순으로 정렬해서 페이징 조회
    Page<Story> findAllByUserWithFavoriteOrderByStatusAndFavorite(
            @Param("user") User user,
            Pageable pageable
    );

    // 특정 사용자가 작성한 스토리 조회
    List<Story> findByUser(User user);
}