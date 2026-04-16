package org.dev.hehe.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.dev.hehe.config.auth.LoginUser;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI 3) 설정
 *
 * 접속 URL : http://localhost:8080/swagger-ui/index.html
 * 상단 select box에서 도메인별 그룹 전환 가능
 *
 * 그룹 구성:
 *  - 전체      : /api/v1/** 전체
 *  - Common    : /api/v1/common/**
 *  - Article   : /api/v1/articles/**
 *  - User/Auth : /api/v1/auth/**, /api/v1/users/**
 *  - Hospital  : /api/v1/hospitals/**, /api/v1/equipments/** (Equipment·Procedure 포함)
 *  - Schedule  : /api/v1/schedules/**
 *  - Interaction : /api/v1/bookmarks/**
 */
@Configuration
public class SwaggerConfig {

    /**
     * @LoginUser 어노테이션이 붙은 파라미터(userId)를 Swagger에서 숨김 처리
     * FE가 userId를 직접 입력하지 않도록 UI에서 제거
     */
    @PostConstruct
    public void hideLoginUserFromSwagger() {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginUser.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HEHE API")
                        .description("HEHE 서비스 API 문서")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /** 전체 API */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00. 전체")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /** 공통 API */
    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("01. Common")
                .pathsToMatch("/api/v1/common/**")
                .build();
    }

    /** 유저 / 인증 */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02. User Auth")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/users/**")
                .build();
    }

    /** 병원 / 기기 / 시술 (Hospital 도메인 통합 그룹: Hospital + Equipment + Procedure) */
    @Bean
    public GroupedOpenApi hospitalApi() {
        return GroupedOpenApi.builder()
                .group("03. Hospital")
                .pathsToMatch("/api/v1/hospitals/**", "/api/v1/equipments/**", "/api/v1/procedures/**")
                .build();
    }

    /** 캘린더 / 일정 */
    @Bean
    public GroupedOpenApi scheduleApi() {
        return GroupedOpenApi.builder()
                .group("04. Schedule")
                .pathsToMatch("/api/v1/schedules/**")
                .build();
    }

    /** 찜 / 인터랙션 */
    @Bean
    public GroupedOpenApi interactionApi() {
        return GroupedOpenApi.builder()
                .group("05. Interaction")
                .pathsToMatch("/api/v1/bookmarks/**")
                .build();
    }

    /** 추천 아티클 */
    @Bean
    public GroupedOpenApi articleApi() {
        return GroupedOpenApi.builder()
                .group("06. Article")
                .pathsToMatch("/api/v1/articles/**")
                .build();
    }

}