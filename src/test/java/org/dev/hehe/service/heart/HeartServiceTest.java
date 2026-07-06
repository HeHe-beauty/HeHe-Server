package org.dev.hehe.service.heart;

import org.dev.hehe.dto.heart.HeartResponse;
import org.dev.hehe.mapper.heart.HeartMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * HeartService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HeartService 테스트")
class HeartServiceTest {

    @Mock
    private HeartMapper heartMapper;

    @InjectMocks
    private HeartService heartService;

    @Test
    @DisplayName("하트 추가 후 누적 수 반환")
    void addHeart_success() {
        // given
        given(heartMapper.countAll()).willReturn(10L);

        // when
        HeartResponse response = heartService.addHeart();

        // then
        verify(heartMapper).insert();
        assertThat(response.getTotal()).isEqualTo(10L);
    }

    @Test
    @DisplayName("누적 하트 수 조회")
    void getTotal_success() {
        // given
        given(heartMapper.countAll()).willReturn(5L);

        // when
        HeartResponse response = heartService.getTotal();

        // then
        assertThat(response.getTotal()).isEqualTo(5L);
    }
}
