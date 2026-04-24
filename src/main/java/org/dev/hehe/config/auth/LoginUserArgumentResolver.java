package org.dev.hehe.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link LoginUser} 어노테이션이 붙은 컨트롤러 파라미터에 JWT의 userId를 주입하는 ArgumentResolver
 *
 * <p>JwtAuthenticationFilter에서 SecurityContext에 설정한 principal(Long userId)을 꺼내 주입한다.</p>
 * <p>인증되지 않은 요청이면 {@code CommonException(ErrorCode.UNAUTHORIZED)} 발생</p>
 */
@Slf4j
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * @LoginUser 어노테이션이 붙어 있고 파라미터 타입이 Long인 경우만 처리
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && Long.class.equals(parameter.getParameterType());
    }

    /**
     * SecurityContext에서 userId(Long)를 꺼내 반환
     *
     * @throws CommonException UNAUTHORIZED — 미인증 또는 principal이 Long이 아닌 경우
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long userId)) {
            return null;
        }

        return userId;
    }
}