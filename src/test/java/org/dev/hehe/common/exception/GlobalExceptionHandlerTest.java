package org.dev.hehe.common.exception;

import org.dev.hehe.common.response.ApiResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler 단위 테스트
 */
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("업로드 용량 초과 예외 - 400(C002)과 안내 메시지로 변환")
    void handleMaxUploadSizeExceeded_returns400WithMessage() {
        // given
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(5 * 1024 * 1024L);

        // when
        ResponseEntity<ApiResult<Void>> response = handler.handleMaxUploadSizeExceeded(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("C002");
        assertThat(response.getBody().getMessage()).isEqualTo("파일 크기가 허용된 최대 용량을 초과했습니다.");
    }
}
