package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
}