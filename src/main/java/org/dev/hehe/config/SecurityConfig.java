package org.dev.hehe.config;

import lombok.RequiredArgsConstructor;
import org.dev.hehe.config.jwt.JwtAuthenticationFilter;
import org.dev.hehe.config.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정
 * - JWT 기반 Stateless 인증
 * - 인증 불필요 경로: /api/v1/auth/**, /api/v1/common/**, Swagger
 * - 나머지 경로: JWT 검증 필수
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (REST API, JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인 / HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Stateless 세션 (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 경로별 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요 경로
                        .requestMatchers("/health").permitAll()       // ALB 헬스체크
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/common/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/hospitals/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/equipments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/legal-documents/**").permitAll()
                        .requestMatchers("/api/v1/hearts/**").permitAll()
                        .requestMatchers("/api/v1/upload/**").permitAll() // TODO: 테스트 완료 후 인증 필요로 변경
                        // Swagger UI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 나머지 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * CORS 허용 오리진 및 메서드 설정
     * - 로컬 개발: http://localhost:5173
     * - 운영: https://www.hehehe.kr, https://hehehe.kr
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://www.hehehe.kr",
                "https://hehehe.kr"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}