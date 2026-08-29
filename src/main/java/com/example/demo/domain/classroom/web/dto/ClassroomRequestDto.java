package com.example.demo.domain.classroom.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ClassroomRequestDto {

    @Getter
    @NoArgsConstructor
    public static class CreateClassroomRequest {
        @NotBlank(message = "학급명은 필수입니다.")
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class JoinClassroomRequest {
        @NotBlank(message = "학급 코드는 필수입니다.")
        private String code;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateAssignmentRequest {
        @NotBlank(message = "과제명은 필수입니다.")
        private String title;

        private String description;

        @NotNull(message = "마감기한은 필수입니다.")
        private LocalDateTime dueAt;

        @NotBlank(message = "공통 프롬프트는 필수입니다.")
        private String commonPrompt;
    }
}
