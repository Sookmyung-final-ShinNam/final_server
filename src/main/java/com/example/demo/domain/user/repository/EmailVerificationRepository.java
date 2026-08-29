package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.EmailVerification;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByUser(User user);
    void deleteAllByUser(User user);
}