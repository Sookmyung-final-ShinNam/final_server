package com.example.demo.domain.classroom.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.service.command.AssignmentCommandService;
import com.example.demo.domain.classroom.service.command.ClassroomCommandService;
import com.example.demo.domain.classroom.service.query.AssignmentQueryService;
import com.example.demo.domain.classroom.service.query.ClassroomQueryService;
import com.example.demo.domain.classroom.web.dto.ClassroomRequestDto;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
import com.example.demo.domain.conversation.web.dto.ConversationRequestDto;
import com.example.demo.domain.conversation.web.dto.ConversationResponseDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController extends AuthController {

    private final ClassroomCommandService classroomCommandService;
    private final AssignmentCommandService assignmentCommandService;
    private final ClassroomQueryService classroomQueryService;
    private final AssignmentQueryService assignmentQueryService;

    // ─────────────────────────────────────────────────────
    // 이메일 인증
    // ─────────────────────────────────────────────────────

    @Operation(summary = "이메일 인증코드 발송", description = "학급 생성 전 선생님 이메일로 6자리 인증코드를 발송합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmailVerification(
            @Valid @RequestBody ClassroomRequestDto.SendEmailRequest request) {
        User user = getCurrentUser();
        classroomCommandService.sendEmailVerification(user, request.getEmail());
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(summary = "이메일 인증코드 확인", description = "발송된 6자리 코드로 이메일 인증을 완료합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(
            @Valid @RequestBody ClassroomRequestDto.VerifyEmailRequest request) {
        User user = getCurrentUser();
        classroomCommandService.verifyEmail(user, request.getCode());
        return ApiResponse.of(SuccessStatus._OK);
    }

    // ─────────────────────────────────────────────────────
    // 학급 관리 (선생님)
    // ─────────────────────────────────────────────────────

    @Operation(summary = "학급 생성", description = "이메일 인증 후 새 학급을 생성합니다. 고유한 8자리 학급 코드가 발급됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping
    public ApiResponse<ClassroomResponseDto.CreateClassroomResponse> createClassroom(
            @Valid @RequestBody ClassroomRequestDto.CreateClassroomRequest request) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                classroomCommandService.createClassroom(user, request.getName()));
    }

    @Operation(summary = "나의 학급 목록 조회 (선생님)", description = "선생님이 개설한 모든 학급 목록을 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/teacher")
    public ApiResponse<ClassroomResponseDto.TeacherClassroomListResponse> getTeacherClassrooms() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getTeacherClassrooms(user));
    }

    @Operation(summary = "학생 가입 승인", description = "학급에 가입 요청한 학생을 승인합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/{classroomId}/students/{studentId}/approve")
    public ApiResponse<Void> approveStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {
        User user = getCurrentUser();
        classroomCommandService.approveStudent(user, classroomId, studentId);
        return ApiResponse.of(SuccessStatus._OK);
    }

    // ─────────────────────────────────────────────────────
    // 학급 가입 (학생)
    // ─────────────────────────────────────────────────────

    @Operation(summary = "학급 가입 신청", description = "학급 코드를 입력해 가입을 요청합니다. 선생님 승인 후 활동 가능합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/join")
    public ApiResponse<Void> joinClassroom(
            @Valid @RequestBody ClassroomRequestDto.JoinClassroomRequest request) {
        User user = getCurrentUser();
        classroomCommandService.joinClassroom(user, request.getCode());
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(summary = "나의 학급 목록 조회 (학생)", description = "가입/가입 대기 중인 모든 학급을 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/student")
    public ApiResponse<ClassroomResponseDto.StudentClassroomListResponse> getStudentClassrooms() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getStudentClassrooms(user));
    }

    // ─────────────────────────────────────────────────────
    // 학급 상세 (공통 - 역할에 따라 응답 다름)
    // ─────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────
    // 과제
    // ─────────────────────────────────────────────────────

    @Operation(summary = "과제 등록 (선생님)", description = "학급에 과제를 등록합니다. 공통 프롬프트(주제/교훈)가 아이들의 동화 생성에 반영됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/{classroomId}/assignments")
    public ApiResponse<Void> createAssignment(
            @PathVariable Long classroomId,
            @Valid @RequestBody ClassroomRequestDto.CreateAssignmentRequest request) {
        User user = getCurrentUser();
        assignmentCommandService.createAssignment(user, classroomId, request);
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(summary = "학급 과제 목록 조회", description = "특정 학급의 모든 과제를 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}/assignments")
    public ApiResponse<List<ClassroomResponseDto.AssignmentResponse>> getClassroomAssignments(
            @PathVariable Long classroomId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, assignmentQueryService.getClassroomAssignments(user, classroomId));
    }

    @Operation(summary = "내 모든 과제 조회 (선생님)", description = "선생님이 등록한 모든 학급의 과제를 조회합니다. 제출/미제출 인원 포함.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/assignments")
    public ApiResponse<List<ClassroomResponseDto.TeacherAssignmentResponse>> getTeacherAssignments() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, assignmentQueryService.getTeacherAssignments(user));
    }

    @Operation(
            summary = "과제로 동화 시작 (학생)",
            description = """
            과제에 설정된 공통 프롬프트(주제/교훈)가 동화 생성 AI에 자동으로 반영됩니다.
            응답 구조는 기존 동화 시작 API와 동일합니다.
            """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/{classroomId}/assignments/{assignmentId}/start")
    public ApiResponse<ConversationResponseDto.ConversationStartResponseDto> startAssignment(
            @PathVariable Long classroomId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody ConversationRequestDto.ConversationStartRequestDto request) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                assignmentCommandService.startAssignment(user, classroomId, assignmentId, request));
    }

    // ─────────────────────────────────────────────────────
    // 학급 동화 보기
    // ─────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────
    // 도토리
    // ─────────────────────────────────────────────────────

    @Operation(summary = "도토리 충전", description = "도토리 5개를 충전합니다. 총 20개 초과 시 관리자 승인이 필요합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/acorn")
    public ApiResponse<Void> chargeAcorn() {
        User user = getCurrentUser();
        classroomCommandService.chargeAcorn(user);
        return ApiResponse.of(SuccessStatus._OK);
    }
}
