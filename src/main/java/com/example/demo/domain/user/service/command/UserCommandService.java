package com.example.demo.domain.user.service.command;

import com.example.demo.domain.user.entity.User;
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

    /**
     * 임시 코드(tempCode)를 사용하여 사용자 로그인 처리
     * tempCode로 토큰 조회 후 사용자 활성화 및 토큰 반환
     *
     * @param tempCode 임시 코드
     * @return 로그인 결과가 담긴 LoginResult
     */
    LoginResponseDto.LoginResult loginUser(String tempCode);

    /**
     * 사용자 비활성화 (로그아웃)
     *
     * @param user 현재 사용자
     * @return 결과 메시지
     */
    String deactivateUser(User user);

    /**
     * 사용자 탈퇴 (매일 자정에 실제 삭제)
     *
     * @param user 현재 사용자
     * @return 결과 메시지
     */
    String withdrawUser(User user);

    /**
     * Scheduler에서 호출하는 실제 삭제 메서드
     * 연관 엔티티(대화, 토큰)만 삭제
     */
    void deleteUser(Long userId);

}