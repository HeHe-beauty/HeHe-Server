package org.dev.hehe.common.exception;

import lombok.Getter;

/**
 * 서비스 공통 커스텀 예외
 *
 * 비즈니스 로직에서 의도된 예외 발생 시 사용.
 * ErrorCode를 통해 HTTP 상태 코드, 에러 코드, 메시지를 일괄 관리한다.
 *
 * 사용 예시:
 *   throw new CommonException(ErrorCode.ARTICLE_NOT_FOUND);
 */
@Getter
public class CommonException extends RuntimeException {

    private final ErrorCode errorCode;

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}