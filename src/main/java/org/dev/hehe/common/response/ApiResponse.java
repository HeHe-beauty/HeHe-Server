package org.dev.hehe.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.dev.hehe.common.exception.ErrorCode;

/**
 * 공통 API 응답 래퍼
 *
 * 성공 시 : { "success": true, "data": { ... } }
 * 실패 시 : { "success": false, "errorCode": "X000", "message": "..." }
 *
 * @JsonInclude(NON_NULL) 로 null 필드는 JSON 응답에서 자동 제외
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    private ApiResponse(boolean success, T data, String errorCode, String message) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
    }

    /** 성공 응답 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** 실패 응답 - ErrorCode 기반 */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getCode(), errorCode.getMessage());
    }

    /** 실패 응답 - ErrorCode + 커스텀 메시지 (유효성 검사 실패 등) */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode.getCode(), message);
    }
}