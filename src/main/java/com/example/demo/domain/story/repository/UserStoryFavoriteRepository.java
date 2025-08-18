package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.UserStoryFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStoryFavoriteRepository extends JpaRepository<UserStoryFavorite, Long> {
}