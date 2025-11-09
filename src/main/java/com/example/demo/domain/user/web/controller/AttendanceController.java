package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.query.AttendanceQueryService;
import com.example.demo.domain.user.web.dto.AttendanceResponse;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController extends AuthController {

    private final AttendanceQueryService attendanceQueryService;

    // 출석 체크 조회
    @Operation(
            summary = "출석 체크 조회",
            description = """
            - 오늘 출석 여부(1), 특정 연도와 월의 출석 기록 리스트(2), 마지막 보상 교환 날짜(3), 모은 도장 수(4)를 반환
            - 출석 기록 리스트 정렬 기준 : date 오름차순
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping
    public ApiResponse<AttendanceResponse> getAttendances(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, attendanceQueryService.getAttendances(user, year, month));
    }

    // 출석 체크 등록
    @Operation(
            summary = "출석 체크 등록",
            description = "오늘의 출석을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping
    public ApiResponse<AttendanceResponse> checkAttendance() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, attendanceQueryService.checkAttendance(user));
    }

    // 출석 체크 보상
    @Operation(
            summary = "출석 체크 보상 교환",
            description = "10개의 스탬프를 1개의 보상으로 교환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/exchange")
    public ApiResponse<AttendanceResponse> exchangeReward() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, attendanceQueryService.exchangeReward(user));
    }
}
