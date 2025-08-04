package com.example.demo.domain.user.service.query;

import com.example.demo.domain.user.web.dto.LoginResponseDto;

/**
 * 사용자 관련 Query 서비스
 * 사용자 정보 조회, 검색
 */
public interface UserQueryService {

    /**
     * 주어진 tempCode가 존재하는지 확인하고, 해당 토큰 정보 반환
     *
     * @param tempCode 임시 코드
     * @return 토큰 정보가 담긴 LoginResult
     */
    LoginResponseDto.LoginResult findTokenByTempCode(String tempCode);

}