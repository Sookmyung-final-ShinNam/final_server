package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // User와 favorites를 한 번에 fetch
    @Query("select u from User u left join fetch u.favorites where u.id = :userId")
    Optional<User> findByIdWithFavorites(@Param("userId") Long userId);

    List<User> findByStatusAndDeletedAtBetween(User.UserStatus status, LocalDateTime start, LocalDateTime end);
}