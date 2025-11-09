package com.example.demo.domain.user.service.query;

import com.example.demo.apiPayload.code.exception.CustomException;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.example.demo.domain.user.entity.Attendance;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.AttendanceRepository;
import com.example.demo.domain.user.web.dto.AttendanceResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceQueryServiceImpl implements AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;

    // 출석 체크 조회
    @Override
    public AttendanceResponse getAttendances(User user, Integer year, Integer month) {

        // 조회할 연도-월
        LocalDate today = LocalDate.now();
        int targetYear = (year != null) ? year : today.getYear();
        int targetMonth = (month != null) ? month : today.getMonthValue();

        // 유효한 월 범위 확인
        if (targetMonth < 0 || targetMonth > 12) throw new CustomException(ErrorStatus.ATTENDANCE_INVALID_INPUT_VALUE);

        // 조회할 월 계산 (1일 ~ 월 마지막 날짜)
        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // 해당 월의 출석 조회
        List<LocalDate> attendances = attendanceRepository
                .findAllByUserAndAttendedDateBetweenOrderByAttendedDateAsc(user, start, end)
                .stream().map(Attendance::getAttendedDate)
                .toList();

        // 오늘 출석 여부
        boolean todayAttendance = attendances.stream().anyMatch(a -> a.equals(today));

        // 마지막 보상 교환일
        LocalDate lastExchangeDate = attendanceRepository.findTopByUserOrderByExchangedDateDesc(user)
                .map(Attendance::getExchangedDate) // 있으면 exchangedDate 추출
                .orElse(null);               // 없으면 null

        // 총 스탬프 수
        int stamps;
        if (lastExchangeDate == null) {  // 교환 이력이 없으면 지금까지 모든 출석 카운트
            stamps = (int) attendanceRepository.countByUser(user);
        } else {                         // 마지막 날짜 교환 이후 출석 카운트
            stamps = (int) attendanceRepository.countByUserAndAttendedDateAfter(user, lastExchangeDate);
        }

        return new AttendanceResponse(todayAttendance, attendances, lastExchangeDate, stamps);
    }

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
        return getAttendances(user, today.getYear(), today.getMonthValue());
    }

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

        // 반영된 출석 체크 다시 조회
        return getAttendances(user, today.getYear(), today.getMonthValue());
    }
}
