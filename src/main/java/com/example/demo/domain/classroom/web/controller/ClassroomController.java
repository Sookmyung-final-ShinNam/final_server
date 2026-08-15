package com.example.demo.domain.classroom.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.service.command.ClassroomCommandService;
import com.example.demo.domain.classroom.service.query.ClassroomQueryService;
import com.example.demo.domain.classroom.web.dto.ClassroomRequestDto;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController extends AuthController {

    private final ClassroomCommandService classroomCommandService;
    private final ClassroomQueryService classroomQueryService;

    // ─────────────────────────────────────────────────────
    // 선생님 - 학급 관리
    // ─────────────────────────────────────────────────────

    @Operation(summary = "학급 생성 (선생님)", description = "고유한 8자리 학급 코드가 발급됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping
    public ApiResponse<ClassroomResponseDto.CreateClassroomResponse> createClassroom(
            @Valid @RequestBody ClassroomRequestDto.CreateClassroomRequest request) {
        User teacher = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                classroomCommandService.createClassroom(teacher, request.getName()));
    }

    @Operation(summary = "학생 가입 승인 (선생님)", description = "학급에 가입 요청한 학생을 승인합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PatchMapping("/{classroomId}/students/{studentId}/approve")
    public ApiResponse<Void> approveStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {
        User teacher = getCurrentUser();
        classroomCommandService.approveStudent(teacher, classroomId, studentId);
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(summary = "학급 도토리 충전 (선생님)", description = "학급 도토리 5개를 충전합니다. 총 20개 초과 시 관리자 승인이 필요합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PatchMapping("/{classroomId}/charge")
    public ApiResponse<Void> chargeAcorn(
            @PathVariable Long classroomId
    ) {
        User teacher = getCurrentUser();
        classroomCommandService.chargeAcorn(teacher, classroomId);
        return ApiResponse.of(SuccessStatus._OK);
    }

    // ─────────────────────────────────────────────────────
    // 학생 - 학급 관리
    // ─────────────────────────────────────────────────────

    @Operation(summary = "학급 가입 신청 (학생)", description = "학급 코드를 입력해 가입을 요청합니다. 선생님 승인 후 활동 가능합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/join")
    public ApiResponse<Void> joinClassroom(
            @Valid @RequestBody ClassroomRequestDto.JoinClassroomRequest request) {
        User student = getCurrentUser();
        classroomCommandService.joinClassroom(student, request.getCode());
        return ApiResponse.of(SuccessStatus._OK);
    }

    // ─────────────────────────────────────────────────────
    // 공통 조회 기능 (역할에 따라 응답 다름)
    // ─────────────────────────────────────────────────────

    @Operation(summary = "나의 학급 목록 조회 (선생님)", description = "선생님이 개설한 모든 학급 목록을 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/teacher")
    public ApiResponse<ClassroomResponseDto.TeacherClassroomListResponse> getTeacherClassrooms() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getTeacherClassrooms(user));
    }

    @Operation(summary = "나의 학급 목록 조회 (학생)", description = "가입/가입 대기 중인 모든 학급을 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/student")
    public ApiResponse<ClassroomResponseDto.StudentClassroomListResponse> getStudentClassrooms() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getStudentClassrooms(user));
    }

    @Operation(
            summary = "학급 상세 조회",
            description = """
            선생님: 가입 요청 중인 학생 포함 전체 목록 조회 (가입 상태 구분)
            학생: 승인된 학생 목록만 조회, 미승인 학급은 접근 불가
            """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}")
    public ApiResponse<Object> getClassroomDetail(@PathVariable Long classroomId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getClassroomDetail(user, classroomId));
    }

    @Operation(summary = "학급 동화 조회", description = "N번째 과제(week)로 만든 학급 전체 동화를 조회합니다. 보관함과 동일한 구조로 반환됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}/stories")
    public ApiResponse<CompletedCharacterResponse.CharacterListResponse> getClassroomStories(
            @PathVariable Long classroomId,
            @RequestParam Integer week) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                classroomQueryService.getClassroomStories(user, classroomId, week));
    }
}
