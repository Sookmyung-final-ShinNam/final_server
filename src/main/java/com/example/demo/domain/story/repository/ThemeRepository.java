package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    /**
     * 테마 이름으로 테마를 조회합니다.
     *
     * @param name 테마 이름
     * @return 테마 엔티티
     */
    Optional<Theme> findByName(String name);
}