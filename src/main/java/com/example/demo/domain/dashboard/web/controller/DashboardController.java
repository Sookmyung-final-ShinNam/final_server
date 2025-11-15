package com.example.demo.domain.dashboard.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.dashboard.service.command.DashboardCommandService;
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

    private final DashboardCommandService commandService;

    /**
     * Story 기반으로 Dashboard 업데이트
     */
    @Operation(summary = "스토리 기반 대시보드 업데이트")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/update")
    public ApiResponse<DashboardResponse> update(@RequestParam Long storyId) {
        User user = getCurrentUser();
        DashboardResponse response = commandService.updateByStory(storyId, user);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

}