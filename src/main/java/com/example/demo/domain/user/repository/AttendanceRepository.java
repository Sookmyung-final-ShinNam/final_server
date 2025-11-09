package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.Attendance;
import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * 특정 유저의 월별 출석 체크 조회
     * - findAllBy: 모든 Attendance 조회
     * - User And AttendedDateBetween: 유저와 두 attendedDate 사이 조건
     * - OrderByAttendedDateAsc: attendedDate 오름차순 정렬
     */
    List<Attendance> findAllByUserAndAttendedDateBetweenOrderByAttendedDateAsc(User user, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 유저의 마지막 보상 날짜 조회
     * - findTopBy: 맨 위 Attendance 하나 조회
     * - User: 유저 조건
     * - OrderByExchangedDateDesc: exchangedDate 내림차순 정렬
     */
    Optional<Attendance> findTopByUserOrderByExchangedDateDesc(User user);

    // 특정 유저의 특정 날짜의 출석 조회
    Optional<Attendance> findByUserAndAttendedDate(User user, LocalDate attendedDate);

    // 특정 유저의 스탬프 수 카운트 (마지막 교환 날짜 X)
    long countByUser(User user);

    // 특정 유저의 스탬프 수 카운트 (마지막 교환 날짜 있다면, 그 이후부터)
    long countByUserAndAttendedDateAfter(User user, LocalDate startDate);

    // 특정 유저의 오늘 출석체크 조회
    boolean existsByUserAndAttendedDate(User user, LocalDate todayDate);
}
