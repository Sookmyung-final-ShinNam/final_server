package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.user.entity.User;
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

    @Operation(summary = "사용자 활성화 및 토큰 조회 (회원가입 겸 로그인)",
            description = """
                    tempToken 을 통해 현재 사용자를 활성화 상태로 변경하고, 새로운 액세스/리프레쉬 토큰을 반환합니다.\n\n
                    **최초 호출(회원가입)**인지 **이후 호출(로그인)**인지는 서버가 자동으로 판별합니다.
                    
                    1. 회원가입 - role 파라미터로 학생/선생님 역할을 확정합니다.
                        - role=BASIC (학생, 기본값) : 별도 인증 절차 없이 바로 가입 완료
                        - role=TEACHER (선생님) : 가입 완료 전 이메일 인증이 반드시 선행되어야 합니다.
                        - role=ADMIN (관리자) : 관리자 역할은 무시되며, BASIC(학생)으로 고정됩니다.
                    
                    2. 로그인 - role 파라미터는 무시되며, 기존에 확정된 역할 그대로 로그인이 진행됩니다.
                    """)
    @Parameter(name = "tempCode", description = "사용자 활성화를 위한 임시 코드", required = true)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PatchMapping("/login")
    public ApiResponse<LoginResponseDto.LoginResult> activateUserAndGetToken(
            @RequestParam("tempCode") String tempCode,
            @RequestParam(value = "role", required = false) User.UserGrade role // role=TEACHER(선생님)일 때만 사용
    ) {
        return ApiResponse.of(SuccessStatus._OK, userCommandService.loginUser(tempCode, role));
    }

}