package com.example.demo.global.web.controller;

import com.example.demo.apiPayload.ApiResponse;
import com.example.demo.apiPayload.status.SuccessStatus;
import com.example.demo.domain.user.entity.User;
import com.example.demo.global.security.AuthController;
import com.example.demo.global.web.dto.HomeResponse;
import com.example.demo.global.service.query.HomeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController extends AuthController {

    private final HomeQueryService homeQueryService;

    @Operation(summary = "홈화면", description = "사용자 정보와 최대 5명의 즐겨찾기 캐릭터 정보를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
    })
    @GetMapping("/api/home")
    public ApiResponse<HomeResponse> getHome() {
        User user = getCurrentUser();
        return ApiResponse.of(SuccessStatus._OK, homeQueryService.getHomeData(user));
    }

}