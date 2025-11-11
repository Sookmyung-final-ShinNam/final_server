package com.example.demo.domain.user.service.command;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.web.dto.AttendanceResponse;

public interface AttendanceCommandService {

    /**
     * 출석 체크 등록
     * @param user 현재 로그인한 사용자
     */
    AttendanceResponse checkAttendance(User user);

    /**
     * 출석 체크 보상 교환
     * @param user 현재 로그인한 사용자
     */
    AttendanceResponse exchangeReward(User user);
}
