package com.example.demo.domain.user.service.command;

import com.example.demo.domain.user.web.dto.LoginResponseDto;

/**
 * 사용자 관련 Command 서비스
 * 사용자 정보 생성, 업데이트
 */
public interface UserCommandService {

    /**
     * 사용자 정보를 등록 or 업데이트
     * 신규 사용자인 경우 등록하고, 기존 사용자인 경우 업데이트
     *
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @return 신규 회원 여부와 tempCode
     */
    LoginResponseDto.Oauth2Result registerOrUpdateUser(String email, String nickname);

}