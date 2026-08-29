package com.example.demo.domain.classroom.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.config.SwaggerConfig;
import com.example.demo.domain.classroom.service.command.ClassroomCommandService;
import com.example.demo.domain.classroom.service.query.ClassroomQueryService;
import com.example.demo.domain.classroom.web.dto.ClassroomRequestDto;
import com.example.demo.domain.classroom.web.dto.ClassroomResponseDto;
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

    @Operation(summary = "학급 목록 조회",
            description = """
                    모든 학급 목록을 조회합니다. **역할**에 따라 응답이 다릅니다.
                    
                    1. role=TEACHER (선생님)
                        - 사용자 닉네임, 학급 이름, 학급 코드, 학급 인원 수
                    
                    2. role=BASIC (학생)
                        - 위 필드 + 학급 가입 상태 (승인/대기) 필드가 추가됨
                    """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping
    public ApiResponse<?> getTeacherClassrooms() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getClassrooms(user));
    }

    @Operation(
            summary = "학급 상세 조회",
            description = """
            특정 학급의 상세 내용 및 가입된 학생을 조회합니다. **역할**에 따라 응답이 다릅니다.
            
            1. role=TEACHER (선생님)
                - 학생 리스트 조회 시, 가입 요청 중인 학생도 함께 조회 가능
                - 가입 승인 처리는 별도 API(PATCH /api/classrooms/{classroomId}/students/{studentId}/approve) 사용
            
            2. role=BASIC (학생)
                - 가입 요청 중인 학급은 조회 불가
                - 학생 리스트 조회 시, 가입 요청 중인 학생은 조회 불가
            """
    )
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"))
    @GetMapping("/{classroomId}")
    public ApiResponse<?> getClassroomDetail(@PathVariable Long classroomId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, classroomQueryService.getClassroomDetail(user, classroomId));
    }
}
