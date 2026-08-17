package com.example.demo.domain.user.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.config.SwaggerConfig;
import com.example.demo.domain.user.service.command.EmailVerificationCommandService;
import com.example.demo.domain.user.web.dto.EmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = SwaggerConfig.Tags.USER_PERMIT)
@RestController
@RequestMapping("/api/permit/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationCommandService emailVerificationCommandService;

    @Operation(summary = "이메일 인증코드 발송",
            description = """
                    선생님 회원가입 시, 입력한 이메일로 인증코드를 발송합니다. **재요청 시 기존 인증 시도는 무효화됩니다.**
                    
                    - tempCode: 카카오 로그인 이후 받은 임시 토큰
                    - email: 인증할 선생님 이메일
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/send")
    public ApiResponse<Void> sendEmailCode(
            @RequestBody @Valid EmailVerificationRequest.Send request
    ) {
        emailVerificationCommandService.sendCode(request.getTempCode(), request.getEmail());
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(summary = "이메일 인증코드 검증",
            description = """
            선생님 회원가입 시, 발송된 인증코드를 검증하여 이메일 인증을 완료합니다.
            
            - tempCode: 카카오 로그인 이후 받은 임시 토큰
            - code: 이메일로 발송된 인증코드
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/verify")
    public ApiResponse<Void> verifyEmailCode(
            @RequestBody @Valid EmailVerificationRequest.Verification request
    ) {
        emailVerificationCommandService.verifyCode(request.getTempCode(), request.getCode());
        return ApiResponse.of(SuccessStatus._OK);
    }
}