package com.example.demo.global.service.query;

import com.example.demo.domain.user.entity.User;
import com.example.demo.global.web.dto.HomeResponse;

public interface HomeQueryService {

    /**
     * 홈화면 데이터 조회
     * @param user 현재 사용자
     * @return HomeResponse 홈화면 응답 DTO
     */
    HomeResponse getHomeData(User user);
}