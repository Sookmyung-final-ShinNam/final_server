package com.example.demo.domain.dashboard.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.dashboard.service.query.DashboardQueryService;
import com.example.demo.domain.dashboard.web.dto.DashboardResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController extends AuthController {

    private final DashboardQueryService queryService;

    /**
     * 현재 유저의 Dashboard 조회
     */
    @Operation(summary = "현재 유저 대시보드 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "대시보드 없음")
    })
    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        User user = getCurrentUser();
        DashboardResponse response = queryService.getDashboard(user);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

}