package com.example.demo.domain.classroom.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ClassroomRequestDto {

    @Getter
    @NoArgsConstructor
    public static class SendEmailRequest {
        @NotEmpty(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    public static class VerifyEmailRequest {
        @NotEmpty(message = "인증코드는 필수입니다.")
        private String code;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateClassroomRequest {
        @NotEmpty(message = "학급명은 필수입니다.")
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class JoinClassroomRequest {
        @NotEmpty(message = "학급 코드는 필수입니다.")
        private String code;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateAssignmentRequest {
        @NotEmpty(message = "과제명은 필수입니다.")
        private String title;

        private String description;

        @NotNull(message = "마감기한은 필수입니다.")
        private LocalDateTime dueAt;

        @NotEmpty(message = "공통 프롬프트는 필수입니다.")
        private String commonPrompt;
    }
}
