package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Token;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByAccessToken(String accessToken);
    Optional<Token> findByRefreshToken(String refreshToken);
    boolean existsByTempCode(String tempCode);
    void deleteAllByUser(User user);
    Optional<Token> findByTempCode(String tempCode);
}