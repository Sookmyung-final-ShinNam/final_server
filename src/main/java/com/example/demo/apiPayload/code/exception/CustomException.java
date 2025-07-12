package com.example.demo.apiPayload.code.exception;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.ErrorStatus;
import org.springframework.http.ResponseEntity;

public class CustomException extends RuntimeException {

    private final ErrorStatus errorStatus;

    // 생성자에서 ErrorStatus를 받음
    public CustomException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }

    // ErrorStatus 반환 메서드
    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    // 제네릭을 추가하여 ApiResponse의 타입을 동적으로 처리
    public static <T> ResponseEntity<ApiResponse<T>> createErrorResponse(ErrorStatus errorStatus, T data) {
        return ResponseEntity
                .status(errorStatus.getHttpStatus())  // 상태 코드에 맞게 응답 반환
                .body(ApiResponse.onFailure(errorStatus, data));  // ApiResponse 반환
    }

}