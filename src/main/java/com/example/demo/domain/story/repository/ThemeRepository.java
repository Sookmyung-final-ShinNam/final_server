package com.example.demo.domain.story.repository;

import com.example.demo.domain.story.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}