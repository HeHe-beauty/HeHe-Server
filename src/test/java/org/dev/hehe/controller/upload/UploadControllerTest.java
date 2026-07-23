package org.dev.hehe.controller.upload;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.service.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UploadController 단위 테스트
 * - @WebMvcTest: Controller + 멀티파트 설정(application.yml의 max-file-size)까지 로드
 */
@WebMvcTest(UploadController.class)
@Import(SecurityConfig.class)
@DisplayName("UploadController 테스트")
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "small-content".getBytes());
        given(s3Service.upload(any(), anyString())).willReturn("https://hehe-bucket.s3.ap-northeast-2.amazonaws.com/articles/uuid_test.jpg");

        // when & then
        mockMvc.perform(multipart("/api/v1/upload/image").file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileUrl").value("https://hehe-bucket.s3.ap-northeast-2.amazonaws.com/articles/uuid_test.jpg"));
    }

    @Test
    @DisplayName("허용되지 않는 파일 형식이면 400")
    void uploadImage_invalidContentType_returns400() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", "content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/v1/upload/image").file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    @Test
    @DisplayName("UploadController 자체 검증 기준(5MB) 초과 시 400")
    void uploadImage_exceedsAppLevelMaxSize_returns400() throws Exception {
        // given: UploadController.MAX_FILE_SIZE(5MB)를 넘는 파일
        // 참고: MockMvc의 multipart()는 실제 서블릿 컨테이너를 안 거쳐서 spring.servlet.multipart.max-file-size
        // 단에서 걸리는 MaxUploadSizeExceededException까지는 재현이 안 되고, 컨트롤러 자체 검증에서 걸림
        // (MaxUploadSizeExceededException 핸들러 자체는 GlobalExceptionHandlerTest에서 직접 검증)
        byte[] oversized = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", oversized);

        // when & then
        mockMvc.perform(multipart("/api/v1/upload/image").file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }
}
