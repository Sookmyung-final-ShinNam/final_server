package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.UserCharacterFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCharacterFavoriteRepository extends JpaRepository<UserCharacterFavorite, Long> {
}