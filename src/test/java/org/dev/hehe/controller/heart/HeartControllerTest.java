package org.dev.hehe.controller.heart;

import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.heart.HeartResponse;
import org.dev.hehe.service.heart.HeartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HeartController 단위 테스트
 */
@WebMvcTest(HeartController.class)
@Import(SecurityConfig.class)
@DisplayName("HeartController 테스트")
class HeartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private HeartService heartService;

    @Test
    @DisplayName("POST /api/v1/hearts - 하트 추가 성공")
    void addHeart_success() throws Exception {
        // given
        given(heartService.addHeart()).willReturn(HeartResponse.of(42L));

        // when & then
        mockMvc.perform(post("/api/v1/hearts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(42));
    }

    @Test
    @DisplayName("GET /api/v1/hearts - 하트 수 조회 성공")
    void getTotal_success() throws Exception {
        // given
        given(heartService.getTotal()).willReturn(HeartResponse.of(42L));

        // when & then
        mockMvc.perform(get("/api/v1/hearts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(42));
    }
}
