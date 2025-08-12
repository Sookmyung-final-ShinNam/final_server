package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BackgroundRepository extends JpaRepository<Background, Long> {

    /**
     * 배경 이름으로 배경을 조회합니다.
     *
     * @param name 배경 이름
     * @return 배경 엔티티
     */
    Optional<Background> findByName(String name);
}