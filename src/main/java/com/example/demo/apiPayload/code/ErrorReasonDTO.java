package com.example.demo.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorReasonDTO {

    private final HttpStatus httpStatus;  // HTTP 상태 코드

    private final boolean isSuccess;      // 성공 여부
    private final String code;            // 에러 코드
    private final String message;         // 에러 메시지

}