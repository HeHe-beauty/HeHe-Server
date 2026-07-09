package org.dev.hehe.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소셜 로그인 응답 DTO
 *
 * <p>exists=false면 가입되지 않은 유저라는 뜻이며, 이 경우 accessToken/refreshToken/user는
 * null이라 응답에서 아예 빠진다({@code @JsonInclude(NON_NULL)}). FE는 exists를 보고
 * false면 회원가입 절차(동의 화면)로, true면 로그인 완료로 분기하면 된다.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "소셜 로그인 응답")
public record AuthLoginResponse(

        @Schema(description = "가입된 유저 존재 여부. false면 미가입 - 회원가입 절차로 진행", example = "true")
        boolean exists,

        @Schema(description = "앱 Access Token (1시간 유효), exists=false면 미노출", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "앱 Refresh Token (14일 유효), exists=false면 미노출", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "유저 정보, exists=false면 미노출")
        UserInfo user
) {

        /** 가입된 유저 로그인/가입 성공 응답 */
        public static AuthLoginResponse of(String accessToken, String refreshToken, UserInfo user) {
                return new AuthLoginResponse(true, accessToken, refreshToken, user);
        }

        /** 미가입 유저 - 로그인 실패(회원가입 필요) 응답 */
        public static AuthLoginResponse notFound() {
                return new AuthLoginResponse(false, null, null, null);
        }

        @Schema(description = "유저 기본 정보")
        public record UserInfo(

                @Schema(description = "유저 ID", example = "1")
                Long userId,

                @Schema(description = "닉네임", example = "홍길동")
                String nickname
        ) {}
}