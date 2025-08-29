package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.command.UserCommandService;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends AuthController {

    private final UserCommandService userCommandService;

    @Operation(summary = "로그아웃",
            description = "사용자를 비활성화 상태로 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PatchMapping("/logout")
    public ApiResponse<String> logout() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, userCommandService.deactivateUser(user));
    }

}