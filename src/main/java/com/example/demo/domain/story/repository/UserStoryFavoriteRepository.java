package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.UserStoryFavorite;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStoryFavoriteRepository extends JpaRepository<UserStoryFavorite, Long> {

    // User 기준으로 모든 즐겨찾기 삭제
    void deleteAllByUser(User user);
}