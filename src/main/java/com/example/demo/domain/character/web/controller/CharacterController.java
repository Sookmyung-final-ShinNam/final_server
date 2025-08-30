package com.example.demo.domain.character.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.character.service.command.CharacterCommandService;
import com.example.demo.domain.character.service.query.CharacterQueryService;
import com.example.demo.domain.character.web.dto.CompletedCharacterResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController extends AuthController {

    private final CharacterQueryService characterQueryService;
    private final CharacterCommandService characterCommandService;

    @Operation(
            summary = "캐릭터 전체 조회",
            description = """
                    - 12개씩 페이징
                    - 페이징 기준 : 관심동화 / createTime 빠른 순서 
                    - 완성된 캐릭터만 반환
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping
    public ApiResponse<CompletedCharacterResponse.CharacterListResponse> getCompletedCharacters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, characterQueryService.getCompletedCharacters(user, page, size));
    }

    @Operation(
            summary = "캐릭터 상세 조회",
            description = """
                - 단일 캐릭터 상세 정보 조회
                - 완료된 캐릭터만 조회 가능
                - 관련된 동화 정보 포함
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/{characterId}")
    public ApiResponse<CompletedCharacterResponse.Detail> getCharacterDetail(
            @PathVariable Long characterId
    ) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, characterQueryService.getCharacterDetail(user, characterId));
    }

    @Operation(summary = "관심 캐릭터 등록")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @PostMapping("/{characterId}/favorite")
    public ApiResponse<String> addFavorite(@PathVariable Long characterId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, characterCommandService.addFavorite(user, characterId));
    }

    @Operation(summary = "관심 캐릭터 취소")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/{characterId}/favorite")
    public ApiResponse<String> removeFavorite(@PathVariable Long characterId) {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, characterCommandService.removeFavorite(user, characterId));
    }

}