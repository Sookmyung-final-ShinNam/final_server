package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.StoryCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {
}