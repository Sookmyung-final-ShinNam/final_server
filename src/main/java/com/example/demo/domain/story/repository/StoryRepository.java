package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Story;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findByStorySessions_Id(Long sessionId);

    @Query("""
        SELECT s FROM Story s
        LEFT JOIN s.userStoryFavorites f WITH f.user = :user
        WHERE s.user = :user
        ORDER BY 
            s.status ASC,                             
            CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END DESC,  
            s.createdAt DESC                          
    """)
    // 진행 중 먼저 / 즐겨찾기 우선 / 생성일 최신순
    Page<Story> findAllByUserWithFavoriteOrderByStatusAndFavorite(
            @Param("user") User user,
            Pageable pageable
    );

}