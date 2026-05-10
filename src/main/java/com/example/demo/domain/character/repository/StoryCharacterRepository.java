package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {

    // 특정 유저의 완료된 스토리의 모든 캐릭터 조회
    @Query("""
        SELECT c 
        FROM StoryCharacter c
        JOIN FETCH c.story s
            WHERE s.user = :user
                AND s.storyStatus IN :success
    """)
    List<StoryCharacter> findByUserAndStoryStatus(
            @Param("user") User user,
            @Param("success") List<Story.StoryStatus> successStatus
    );

    // 완료된 특정 캐릭터 상세 조회
    @Query("""
        SELECT c
        FROM StoryCharacter c
            JOIN FETCH c.story s
            LEFT JOIN FETCH s.storyPages
                WHERE c.id = :id
                     AND s.storyStatus IN :success
    """)
    Optional<StoryCharacter> findByIdAndStoryStatus(
            @Param("id") Long characterId,
            @Param("success") List<Story.StoryStatus> successStatus
    );
}