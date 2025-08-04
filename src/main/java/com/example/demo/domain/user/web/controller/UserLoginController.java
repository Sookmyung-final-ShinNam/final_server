package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.user.service.command.UserCommandService;
import com.example.demo.domain.user.web.dto.LoginResponseDto;
import com.example.demo.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permit")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserCommandService userCommandService;

    @Operation(summary = "사용자 활성화 및 토큰 조회",
            description = "tempToken 을 통해 현재 사용자를 활성화 상태로 변경하고, 새로운 액세스/리프레쉬 토큰을 반환합니다.")
    @Parameter(name = "tempCode", description = "사용자 활성화를 위한 임시 코드", required = true)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PatchMapping("/login")
    public ApiResponse<LoginResponseDto.LoginResult> activateUserAndGetToken(
            @RequestParam("tempCode") String tempCode
    ) {
        return ApiResponse.of(SuccessStatus._OK, userCommandService.loginUser(tempCode));
    }

}