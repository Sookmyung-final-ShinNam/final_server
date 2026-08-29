package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.config.SwaggerConfig;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.command.UserCommandService;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = SwaggerConfig.Tags.USER_AUTH)
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

    @Operation(summary = "사용자 권한 확인",
            description = "현재 로그인한 사용자가 지정한 권한을 가지고 있는지 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/check-role")
    public ApiResponse<Boolean> checkRole(
            @RequestParam("role") User.UserGrade role
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, user.getGrade() == role);
    }
}