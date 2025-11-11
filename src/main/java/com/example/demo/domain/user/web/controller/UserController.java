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

    @Operation(summary = "회원 탈퇴",
            description = "사용자를 탈퇴 처리합니다. (매일 자정에 실제 삭제. 삭제 전에 로그인시 재활성화 가능)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/withdraw")
    public ApiResponse<String> withdraw() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, userCommandService.withdrawUser(user));
    }

    @Operation(summary = "Admin 여부 확인",
            description = "현재 로그인한 사용자가 관리자 권한을 가지고 있는지 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/is-admin")
    public ApiResponse<Boolean> isAdmin() {
        User user = getCurrentUser();
        boolean isAdmin = user.getGrade() == User.UserGrade.ADMIN;
        return ApiResponse.of(SuccessStatus._OK, isAdmin);
    }
}