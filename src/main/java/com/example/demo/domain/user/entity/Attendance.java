package com.example.demo.domain.user.entity;

import com.example.demo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "attendance_entity",
        uniqueConstraints = { // 제약 조건: 출석 날짜 중복 방지
                @UniqueConstraint(columnNames = {"user_id", "attended_date"})}
)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    // 출석 체크한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 출석 체크한 날짜 (yyyy-mm-dd)
    @Column(nullable = false)
    private LocalDate attendedDate;

    // 보상으로 교환한 날짜 (yyyy-mm-dd)
    @Column
    private LocalDate exchangedDate;

    // 출석 체크 생성자
    public Attendance(User user, LocalDate todayDate) {
        this.user = user;
        this.attendedDate = todayDate;
    }

    // 보상으로 교환한 날짜 업데이트
    public void updateExchangeDate(LocalDate todayDate) {
        this.exchangedDate = todayDate;
    }
}
