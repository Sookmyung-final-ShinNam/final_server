package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.CharacterAppearance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterAppearanceRepository extends JpaRepository<CharacterAppearance, Long> {
}