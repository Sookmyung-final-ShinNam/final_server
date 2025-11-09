package com.example.demo.domain.user.service.query;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.web.dto.AttendanceResponse;

public interface AttendanceQueryService {

    /**
     * 출석 체크 조회
     * @param user 현재 로그인한 사용자
     * @param year 조회할 연도
     * @param month 조회할 월
     */
    AttendanceResponse getAttendances(User user, Integer year, Integer month);

    /**
     * 출석 체크 등록
     * @param user 현재 로그인한 사용자
     */
    AttendanceResponse checkAttendance(User user);

    /**
     * 출석 체크 조회
     * @param user 현재 로그인한 사용자
     */
    AttendanceResponse exchangeReward(User user);
}
