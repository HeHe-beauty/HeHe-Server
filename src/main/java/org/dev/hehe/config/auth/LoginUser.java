package org.dev.hehe.config.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT SecurityContext에서 인증된 유저의 userId(Long)를 컨트롤러 파라미터에 주입하는 어노테이션
 *
 * <p>사용 예:</p>
 * <pre>
 *   public ApiResult<?> someApi(@LoginUser Long userId) { ... }
 * </pre>
 *
 * <p>미인증 요청인 경우 {@code CommonException(ErrorCode.UNAUTHORIZED)} 발생</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}