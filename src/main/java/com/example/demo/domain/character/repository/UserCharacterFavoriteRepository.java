package com.example.demo.domain.character.repository;

import com.example.demo.domain.character.entity.StoryCharacter;
import com.example.demo.domain.character.entity.UserCharacterFavorite;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCharacterFavoriteRepository extends JpaRepository<UserCharacterFavorite, Long> {

    // User 기준으로 모든 즐겨찾기 삭제
    void deleteAllByUser(User user);

    // 특정 사용자와 캐릭터 조합이 이미 존재하는지 여부 확인
    boolean existsByUserAndCharacter(User user, StoryCharacter character);

    // 특정 사용자와 캐릭터 조합으로 즐겨찾기 엔티티 조회
    Optional<UserCharacterFavorite> findByUserAndCharacter(User user, StoryCharacter character);

    // 유저별 최근 즐겨찾기 캐릭터 5개 조회
    List<UserCharacterFavorite> findTop5ByUserOrderByCreatedAtDesc(User user);

    // 특정 사용자의 모든 즐겨찾기 조회
    List<UserCharacterFavorite> findByUser(User user);
}