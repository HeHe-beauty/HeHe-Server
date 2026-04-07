package org.dev.hehe.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 *
 * 모든 컨트롤러에서 발생하는 예외를 일관된 ApiResponse 형태로 변환하여 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 의도적으로 발생시킨 예외 처리
     * ErrorCode에 정의된 HTTP 상태 코드로 응답
     */
    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommonException(CommonException e) {
        log.warn("CommonException - code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(e.getErrorCode()));
    }

    /**
     * @Valid 유효성 검사 실패 예외 처리
     * 첫 번째 필드 에러 메시지를 응답에 포함
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        log.warn("Validation failed - {}", errorMessage);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, errorMessage));
    }

    /**
     * 필수 요청 파라미터 누락 예외 처리 (@RequestParam 필수값 미전달 등)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter - {}", e.getParameterName());
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, e.getParameterName() + " 파라미터가 필요합니다."));
    }

    /**
     * 예상하지 못한 서버 내부 오류 처리
     * 상세 오류는 로그로만 기록하고, 클라이언트에는 일반 메시지만 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}