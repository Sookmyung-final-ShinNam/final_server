package com.example.demo.apiPayload.status;

import com.example.demo.apiPayload.code.BaseErrorCode;
import com.example.demo.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 공통 에러
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러가 발생했습니다. 관리자에게 문의하세요."),
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_404", "잘못된 요청입니다."),
    COMMON_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_405", "인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."),

    // 인증 관련 에러
    OAUTH_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "OAUTH_4001", "OAuth 처리 중 오류가 발생했습니다. 인가 코드와 상태 값을 확인하세요."),
    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "OAUTH_4002", "OAuth 로그인에 실패했습니다. 올바른 자격 증명을 제공하세요."),

    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "TOKEN_4001", "토큰이 누락되었습니다. Authorization 헤더를 확인하세요."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "TOKEN_4002", "토큰이 존재하지 않습니다. 유효한 토큰을 제공하세요."),
    TOKEN_INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_4003", "유효하지 않은 액세스 토큰입니다. 토큰을 확인하고 다시 시도하세요."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "TOKEN_4004", "지원하지 않는 토큰 형식입니다. 올바른 토큰을 제공하세요."),
    TOKEN_CLAIMS_EMPTY(HttpStatus.UNAUTHORIZED, "TOKEN_4005", "토큰의 클레임이 비어 있습니다. 올바른 토큰을 제공하세요."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_4006", "액세스 토큰이 만료되었습니다. 리프레시 토큰을 사용하여 새 액세스 토큰을 발급받으세요."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_4011", "임시 토큰 생성에 실패했습니다. 서버 관리자에게 문의하세요."),

    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMAIL_4001", "이메일을 찾을 수 없습니다. 올바른 이메일을 입력하세요."),

    // 회원 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_4004", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_LOGOUT(HttpStatus.BAD_REQUEST, "USER_4001", "이미 로그아웃된 사용자입니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_4002", "이미 삭제된 사용자입니다."),

    // 관리자 관련 에러
    ADMIN_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "ADMIN_4003", "관리자 권한이 필요한 접근입니다. 관리자 계정으로 로그인하세요."),

    // 대화 관련 에러
    CHAT_GPT_API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_GPT_5001", "ChatGPT API 호출에 실패했습니다. 관리자에게 문의하세요."),
    CHAT_GPT_API_JSON_ESCAPE_FAILED(HttpStatus.BAD_REQUEST, "CHAT_GPT_4001", "ChatGPT API 호출 메시지의 json 형식이 올바르지 않습니다."),
    CHAT_GPT_API_FILE_CALL_FAILED(HttpStatus.BAD_REQUEST, "CHAT_GPT_4002", "ChatGPT API 프롬프트 파일 호출에 실패하였습니다."),
    CHAT_GPT_API_RESPONSE_FAILED(HttpStatus.BAD_GATEWAY, "CHAT_GPT_4003", "ChatGPT API 응답을 파싱하는데 실패하였습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .code(code)
                .message(message)
                .isSuccess(false)
                .build();
    }

}