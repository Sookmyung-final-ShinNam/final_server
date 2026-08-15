package com.example.demo.domain.classroom.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.classroom.service.command.AssignmentCommandService;
import com.example.demo.domain.classroom.service.query.AssignmentQueryService;
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
public class AssignmentController extends AuthController {

    private final AssignmentCommandService assignmentCommandService;
    private final AssignmentQueryService assignmentQueryService;

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

//    @Operation(
//            summary = "과제로 동화 시작 (학생)",
//            description = """
//            과제에 설정된 공통 프롬프트(주제/교훈)가 동화 생성 AI에 자동으로 반영됩니다.
//            응답 구조는 기존 동화 시작 API와 동일합니다.
//            """
//    )
//    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
//    @PostMapping("/{classroomId}/assignments/{assignmentId}/start")
//    public ApiResponse<ConversationResponseDto.ConversationStartResponseDto> startAssignment(
//            @PathVariable Long classroomId,
//            @PathVariable Long assignmentId,
//            @Valid @RequestBody ConversationRequestDto.ConversationStartRequestDto request) {
//        User user = getCurrentUser();
//        return ApiResponse.of(SuccessStatus._OK,
//                assignmentCommandService.startAssignment(user, classroomId, assignmentId, request));
//    }
//
//    @Operation(summary = "학급 동화 조회",
//            description = "N번째 과제(week)로 만든 학급 전체 동화를 조회합니다. 보관함과 동일한 구조로 반환됩니다."
//    )
//    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
//    @GetMapping("/{classroomId}/stories")
//    public ApiResponse<CompletedCharacterResponse.CharacterListResponse> getClassroomStories(
//            @PathVariable Long classroomId,
//            @RequestParam Integer week) {
//        User user = getCurrentUser();
//        return ApiResponse.of(SuccessStatus._OK,
//                classroomQueryService.getClassroomStories(user, classroomId, week));
//    }
}
