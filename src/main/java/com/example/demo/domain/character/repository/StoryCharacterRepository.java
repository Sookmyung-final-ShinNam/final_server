package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.StoryCharacter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {

    // 완료된 캐릭터만 조회 - 페이징용
    Page<StoryCharacter> findByStatus(StoryCharacter.CharacterStatus status, Pageable pageable);

    // 완료된 캐릭터만 조회
    Optional<StoryCharacter> findByIdAndStatus(Long id, StoryCharacter.CharacterStatus status);
}