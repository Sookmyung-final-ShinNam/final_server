package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.StoryCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.domain.user.entity.User;

import java.util.Optional;
import java.util.List;

public interface StoryCharacterRepository extends JpaRepository<StoryCharacter, Long> {

    // 특정 유저의 모든 캐릭터 조회
    List<StoryCharacter> findByStory_User(User user);

    // 완료된 캐릭터만 조회
    Optional<StoryCharacter> findByIdAndStatus(Long id, StoryCharacter.CharacterStatus status);
}