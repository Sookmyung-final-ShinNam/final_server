package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.UserStoryFavorite;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.domain.story.entity.Story;

import java.util.Optional;

@Repository
public interface UserStoryFavoriteRepository extends JpaRepository<UserStoryFavorite, Long> {

    // User 기준으로 모든 즐겨찾기 삭제
    void deleteAllByUser(User user);

    // User와 Story로 즐겨찾기 존재 여부 확인
    boolean existsByUserAndStory(User user, Story story);

    // User와 Story로 즐겨찾기 조회
    Optional<UserStoryFavorite> findByUserAndStory(User user, Story story);

}