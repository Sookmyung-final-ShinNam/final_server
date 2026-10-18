package com.example.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    public static class Tags {
        public static final String USER_PERMIT = "회원가입/로그인";
        public static final String USER_AUTH = "회원 계정";
        public static final String HOME = "홈 화면";
        public static final String CONVERSATION = "AI 동화 생성";
        public static final String CHARACTER = "캐릭터 조회";
        public static final String STORY = "동화 조회";
        public static final String ATTENDANCE = "출석 체크";
        public static final String DASHBOARD = "대시보드 분석";
        public static final String CLASS_ROOM = "나의 학급";
        public static final String ADMIN = "관리자";
    }

    @Bean
    public OpenAPI createOpenAPI() {

        Info apiInfo = new Info()
                .title("장신남 졸프 API")
                .description("장신남 졸프 API 명세서 - 최종본")
                .version("3.0.0");

        String jwtSchemeName = "JWT_TOKEN";

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName,
                        new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));

        List<Tag> tags = List.of(
                new Tag().name(Tags.USER_PERMIT).description("로그인, 회원가입, 선생님 이메일 인증코드 발송 및 검증"),
                new Tag().name(Tags.USER_AUTH).description("로그아웃, 회원 탈퇴, 사용자 상태 조회"),
                new Tag().name(Tags.HOME).description("홈 화면 조회"),
                new Tag().name(Tags.CONVERSATION).description("대화 세션 시작·진행·피드백, 동화 생성, 동영상 생성"),
                new Tag().name(Tags.CHARACTER).description("캐릭터 목록·상세 조회, 즐겨찾기"),
                new Tag().name(Tags.STORY).description("동화 페이지 조회"),
                new Tag().name(Tags.ATTENDANCE).description("출석체크 조회·등록, 보상 교환"),
                new Tag().name(Tags.DASHBOARD).description("대시보드 조회"),
                new Tag().name(Tags.CLASS_ROOM).description("학생 및 선생님 학급·과제 관련"),
                new Tag().name(Tags.ADMIN).description("누락 및 실패 상태 동화 조회, 유튜브 링크 등록")
        );

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(components)
                .tags(tags);
    }

}
