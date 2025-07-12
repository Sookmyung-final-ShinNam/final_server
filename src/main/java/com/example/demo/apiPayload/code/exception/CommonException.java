package com.example.demo.apiPayload.code.exception;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.ErrorStatus;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CommonException {

    // 1. ConstraintViolationException 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));
        logValidationError("ConstraintViolation", errors);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    // 2. MethodArgumentNotValidException 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage()
                ));
        logValidationError("MethodArgumentNotValid", errors);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, errors);
    }

    // 3. 잘못된 요청값 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage());

        Throwable cause = ex.getCause();

        // 잘못된 Enum 값 처리
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            if (invalidFormatException.getTargetType().isEnum()) {

                String fieldName = invalidFormatException.getPath().get(0).getFieldName();
                String invalidValue = invalidFormatException.getValue().toString();
                String enumType = invalidFormatException.getTargetType().getSimpleName();

                log.error("Invalid enum value '{}' for field '{}' of type '{}'",
                        invalidValue, fieldName, enumType);

                return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST,
                        String.format("Invalid value '%s' for field '%s'. Expected one of: %s",
                                invalidValue, fieldName, getEnumValues(invalidFormatException.getTargetType())));
            }
        }

        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST, "Invalid request format");
    }

    // 4. 필수 파라미터 누락 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.error("Missing required parameter: {}", ex.getParameterName());
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST,
                "Required parameter is missing: " + ex.getParameterName());
    }

    // 5. 메서드 파라미터 타입 불일치 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Method argument type mismatch: parameter '{}' should be of type '{}'",
                ex.getName(), ex.getRequiredType().getSimpleName());
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST,
                String.format("Parameter '%s' should be of type '%s'", ex.getName(), ex.getRequiredType().getSimpleName()));
    }

    // 6. 지원하지 않는 HTTP 메서드 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("HTTP method not supported: {}", ex.getMethod());
        return CustomException.createErrorResponse(ErrorStatus.COMMON_BAD_REQUEST,
                "HTTP method '" + ex.getMethod() + "' is not supported");
    }

    // 7. CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        log.error("Custom Error: {} - Code: {} - Status: {}",
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                ex.getErrorStatus().getHttpStatus());

        return CustomException.createErrorResponse(ex.getErrorStatus(), ex.getMessage());
    }

    // 8. 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return CustomException.createErrorResponse(ErrorStatus.COMMON_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

    // 공통 로그 메서드
    private void logValidationError(String type, Map<String, String> errors) {
        log.error("{} Validation Error: {}", type, errors);
    }

    // Enum 값들을 문자열로 반환하는 메서드
    private String getEnumValues(Class<?> enumType) {
        if (enumType.isEnum()) {
            Object[] enumConstants = enumType.getEnumConstants();
            return java.util.Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }

}