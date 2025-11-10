package com.example.demo.domain.user.service.command;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.Attendance;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.AttendanceRepository;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.query.AttendanceQueryService;
import com.example.demo.domain.user.web.dto.AttendanceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandServiceImpl implements AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceQueryService attendanceQueryService;

    // 출석 체크 등록
    @Override
    public AttendanceResponse checkAttendance(User user) {

        // 현재 날짜
        LocalDate today = LocalDate.now();

        // 이미 오늘 출석 체크했는지 확인
        if (attendanceRepository.existsByUserAndAttendedDate(user, today))
            throw new CustomException(ErrorStatus.ATTENDANCE_ALREADY_CHECKED);

        // 출석 체크 생성
        attendanceRepository.save(new Attendance(user, today));

        // 반영된 출석 체크 다시 조회
        return attendanceQueryService.getAttendances(user, today.getYear(), today.getMonthValue());
    }

    // 출석 체크 보상 교환
    @Override
    public AttendanceResponse exchangeReward(User user) {

        // 현재 날짜
        LocalDate today = LocalDate.now();

        // 마지막 보상 교환일 기준 스탬프 수 확인
        LocalDate lastExchangeDate = attendanceRepository.findTopByUserOrderByExchangedDateDesc(user)
                .map(Attendance::getExchangedDate) // 있으면 exchangedDate 추출
                .orElse(null);               // 없으면 null

        int stamps;
        if (lastExchangeDate == null) {  // 교환 이력이 없으면 지금까지 모든 출석 카운트
            stamps = (int) attendanceRepository.countByUser(user);
        } else {                         // 마지막 날짜 교환 이후 출석 카운트
            stamps = (int) attendanceRepository.countByUserAndAttendedDateAfter(user, lastExchangeDate);
        }

        // 스탬프 10개 미만
        if (stamps < 10)
            throw new CustomException(ErrorStatus.ATTENDANCE_EXCHANGE_FAILED);

        // 보상받은 날짜 업데이트
        Attendance todayAttendance = attendanceRepository.findByUserAndAttendedDate(user, today)
                .orElseThrow(() -> new CustomException(ErrorStatus.ATTENDANCE_NOT_CHECKED));
        todayAttendance.updateExchangeDate(today);

        // 유저 포인트 반영
        User attendanceUser = todayAttendance.getUser();
        attendanceUser.setPoints(attendanceUser.getPoints() + 1);

        // 반영된 출석 체크 다시 조회
        return attendanceQueryService.getAttendances(user, today.getYear(), today.getMonthValue());
    }
}
