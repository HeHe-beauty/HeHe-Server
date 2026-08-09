package org.dev.hehe.service.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * S3Service 단위 테스트
 * - 업로드 시 S3 key에 원본 파일명이 들어가지 않고 UUID + 확장자만 사용되는지 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3Service 테스트")
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", "hehe-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "ap-northeast-2");
    }

    @Test
    @DisplayName("업로드 시 원본 파일명 대신 UUID + 확장자로 S3 key를 생성한다")
    void upload_usesUuidInsteadOfOriginalFilename() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "위험한파일명../../etc.jpg", "image/jpeg", "content".getBytes());
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        String fileUrl = s3Service.upload(file, "articles");

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        String key = captor.getValue().key();

        assertThat(key).doesNotContain("위험한파일명");
        assertThat(key).doesNotContain("..");
        assertThat(key).startsWith("articles/");
        assertThat(key).endsWith(".jpg");
        assertThat(fileUrl).contains(key);
    }

    @Test
    @DisplayName("Content-Type이 png/webp면 그에 맞는 확장자를 사용한다")
    void upload_resolvesExtensionByContentType() {
        // given
        MockMultipartFile pngFile = new MockMultipartFile("file", "any.png", "image/png", "content".getBytes());
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willReturn(PutObjectResponse.builder().build());

        // when
        String fileUrl = s3Service.upload(pngFile, "articles");

        // then
        assertThat(fileUrl).endsWith(".png");
    }
}
