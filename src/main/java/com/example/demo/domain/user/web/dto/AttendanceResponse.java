package com.example.demo.domain.user.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class AttendanceResponse {
        private final boolean todayAttendance;   // 오늘 출석 여부
        private final List<LocalDate> attendances; // 특정 달 출석 기록 리스트
        private final LocalDate lastExchangeDate;  // 마지막으로 보상으로 교환받은 날짜
        private final Integer totalStamps;         // 모은 도장 수 (마지막 보상 교환 이후 기준)
}
