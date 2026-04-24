package org.dev.hehe.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.domain.user.User;
import org.dev.hehe.dto.auth.AuthLoginResponse;
import org.dev.hehe.mapper.user.UserMapper;
import org.dev.hehe.service.auth.RedisTokenService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개발 전용 인증 컨트롤러 — local 프로파일에서만 활성화
 *
 * <p>FE OAuth 없이 userId만으로 JWT를 발급하여 API 테스트를 가능하게 한다.
 * {@code @Profile("local")} 로 운영 환경에서는 절대 노출되지 않는다.</p>
 */
@Slf4j
@Profile("local")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final RedisTokenService redisTokenService;

    /**
     * 개발용 로그인 — OAuth 없이 userId로 JWT 직접 발급
     *
     * <p>tb_user에 존재하는 userId를 전달하면 해당 유저의 accessToken과 refreshToken을 반환한다.</p>
     *
     * @param userId 로그인할 유저 ID (tb_user에 존재해야 함)
     * @return accessToken, refreshToken, 유저 정보
     * @throws CommonException U001 (존재하지 않는 userId)
     */
    @PostMapping("/dev-login")
    public ApiResult<AuthLoginResponse> devLogin(@RequestParam Long userId) {
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        redisTokenService.save(userId, refreshToken);

        log.info("[DevAuth] 개발용 로그인 - userId={}, nickname={}", userId, user.getNickname());

        return ApiResult.ok(new AuthLoginResponse(
                accessToken,
                refreshToken,
                new AuthLoginResponse.UserInfo(userId, user.getNickname())
        ));
    }
}
