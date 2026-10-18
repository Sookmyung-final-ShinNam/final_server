package com.example.demo.domain.classroom.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.config.SwaggerConfig;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = SwaggerConfig.Tags.CLASS_ROOM)
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class AssignmentController extends AuthController {

    private final AssignmentCommandService assignmentCommandService;
    private final AssignmentQueryService assignmentQueryService;

    // ─────────────────────────────────────────────────────
    // 선생님 - 과제 관리
    // ─────────────────────────────────────────────────────

    @Operation(summary = "과제 등록 (선생님)", description = "학급에 과제를 등록합니다. 공통 프롬프트(주제/교훈)가 학생들의 동화 생성에 반영됩니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/{classroomId}/assignments")
    public ApiResponse<Void> createAssignment(
            @PathVariable Long classroomId,
            @Valid @RequestBody ClassroomRequestDto.CreateAssignmentRequest request) {
        User user = getCurrentUser();
        assignmentCommandService.createAssignment(user, classroomId, request);
        return ApiResponse.of(SuccessStatus._OK);
    }

    @Operation(
            summary = "과제 제출 현황 조회 (선생님)",
            description = """
                    특정 과제의 학생별 제출 여부를 조회합니다.

                    - 가입 승인된 학생만 조회됩니다. (학생 번호는 학급 상세 조회와 동일, 이름순)
                    - 제출 여부는 **대화 세션 완료(COMPLETED) 여부**로 판단합니다.
                    - 선생님만 조회할 수 있고, 본인 학급의 과제여야 합니다.
                    """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}/assignments/{assignmentId}/submissions")
    public ApiResponse<ClassroomResponseDto.AssignmentSubmissionResponse> getAssignmentSubmissions(
            @PathVariable Long classroomId,
            @PathVariable Long assignmentId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                assignmentQueryService.getAssignmentSubmissions(user, classroomId, assignmentId));
    }

    @Operation(
            summary = "미제출 학생 알림 메일 발송 (선생님)",
            description = """
                    특정 과제를 아직 제출하지 않은 학생들에게 알림 메일을 보냅니다.

                    - 가입 승인된 학생 중 제출(대화 세션 완료)하지 않은 학생에게 **각각** 발송됩니다. (다른 학생의 이메일은 노출되지 않음)
                    - 마감된 과제에는 보낼 수 없습니다.
                    - 같은 과제에는 24시간에 한 번만 보낼 수 있습니다. (스팸 방지)
                    - 메일은 비동기로 발송되므로, 응답의 recipientCount는 발송 요청한 학생 수입니다.
                    """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @PostMapping("/{classroomId}/assignments/{assignmentId}/reminders")
    public ApiResponse<ClassroomResponseDto.AssignmentReminderResponse> remindAssignment(
            @PathVariable Long classroomId,
            @PathVariable Long assignmentId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK,
                assignmentCommandService.remindAssignment(user, classroomId, assignmentId));
    }

    // ─────────────────────────────────────────────────────
    // 학생 - 과제 관리
    // ─────────────────────────────────────────────────────

    @Operation(
            summary = "과제 진행 (학생)",
            description = """
            과제에 설정된 공통 프롬프트(주제/교훈)가 동화 생성에 자동으로 반영됩니다.
            
            동화 생성은 기존 동화 생성 API와 동일한 요청 및 응답 구조를 따릅니다.
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
    // 공통 조회 기능 (역할에 따라 응답 다름)
    // ─────────────────────────────────────────────────────

    @Operation(summary = "학급 과제 목록 조회",
            description = """
                    특정 학급의 모든 과제를 조회합니다. **역할**에 따라 응답이 다릅니다.
                    
                    제출 여부는 **대화 세션 완료(COMPLETED) 여부**로 판단합니다.
                    
                    1. role=TEACHER (선생님)
                        - 각 과제의 제출 상태 현황(제출/미제출 인원)을 확인할 수 있습니다.
                    
                    2. role=BASIC (학생)
                        - 각 과제의 제출 여부를 확인할 수 있습니다.
                    """)
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}/assignments")
    public ApiResponse<?> getClassroomAssignments(@PathVariable Long classroomId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, assignmentQueryService.getClassroomAssignments(user, classroomId));
    }

    @Operation(summary = "학급 동화 목록 조회",
            description = """
                    특정 학급의 모든 동화를 조회합니다.
                    
                    assignmentId로 특정 과제의 동화 조회가 가능합니다. (assignmentId 값 없으면 전체 조회)
                    
                    동화 조회는 기존 보관함(캐릭터 전체 조회 API)과 동일한 구조를 따릅니다.
                        - important(관심 캐릭터 여부)는 모두 false로 고정
                    """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}/stories")
    public ApiResponse<CompletedCharacterResponse.CharacterListResponse> getClassroomStories(
            @PathVariable Long classroomId,
            @RequestParam(required = false) Long assignmentId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, assignmentQueryService.getClassroomStories(user, classroomId, assignmentId));
    }
}
