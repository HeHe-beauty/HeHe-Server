package org.dev.hehe.controller.contact;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.config.SecurityConfig;
import org.dev.hehe.config.jwt.JwtProvider;
import org.dev.hehe.dto.contact.ContactHistoryResponse;
import org.dev.hehe.service.contact.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ContactController 단위 테스트
 */
@WebMvcTest(ContactController.class)
@Import(SecurityConfig.class)
@DisplayName("ContactController 테스트")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setup() {
        given(jwtProvider.getUserIdFromToken(TEST_TOKEN)).willReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("GET /api/v1/contacts - 문의 내역 조회 성공")
    void getContactHistories_success() throws Exception {
        // given
        List<ContactHistoryResponse> mockList = List.of(
                ContactHistoryResponse.builder()
                        .id(1L).hospitalId(101L).hospitalName("강남 제모 클리닉")
                        .contactType("CALL").createdAt(LocalDateTime.of(2026, 4, 22, 10, 30))
                        .build(),
                ContactHistoryResponse.builder()
                        .id(2L).hospitalId(102L).hospitalName("역삼 스킨케어")
                        .contactType("VISIT").createdAt(LocalDateTime.of(2026, 4, 20, 15, 0))
                        .build()
        );

        given(contactService.getContactHistories(TEST_USER_ID)).willReturn(mockList);

        // when & then
        mockMvc.perform(get("/api/v1/contacts")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].hospitalName").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data[0].contactType").value("CALL"))
                .andExpect(jsonPath("$.data[1].contactType").value("VISIT"));
    }

    @Test
    @DisplayName("GET /api/v1/contacts - 문의 내역 없음 (빈 배열 반환)")
    void getContactHistories_empty() throws Exception {
        // given
        given(contactService.getContactHistories(TEST_USER_ID)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/contacts")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/contacts - 인증 토큰 없이 호출 시 4xx")
    void getContactHistories_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/contacts"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/v1/contacts - 문의 저장 성공 (201)")
    void saveContact_success() throws Exception {
        // given
        willDoNothing().given(contactService).saveContact(eq(TEST_USER_ID), any());

        // when & then
        mockMvc.perform(post("/api/v1/contacts")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalId\": 101, \"contactType\": \"CALL\"}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/contacts - 유효하지 않은 contactType 시 400")
    void saveContact_invalidContactType() throws Exception {
        mockMvc.perform(post("/api/v1/contacts")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalId\": 101, \"contactType\": \"INVALID\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/contacts - 인증 토큰 없이 호출 시 4xx")
    void saveContact_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hospitalId\": 101, \"contactType\": \"CALL\"}"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("DELETE /api/v1/contacts/{contactId} - 문의 삭제 성공 (200)")
    void deleteContact_success() throws Exception {
        // given
        willDoNothing().given(contactService).deleteContact(eq(1L), eq(TEST_USER_ID));

        // when & then
        mockMvc.perform(delete("/api/v1/contacts/1")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/contacts/{contactId} - 존재하지 않는 문의 내역 (404)")
    void deleteContact_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.CONTACT_NOT_FOUND))
                .given(contactService).deleteContact(eq(999L), eq(TEST_USER_ID));

        // when & then
        mockMvc.perform(delete("/api/v1/contacts/999")
                        .header("Authorization", "Bearer " + TEST_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CO001"));
    }

    @Test
    @DisplayName("DELETE /api/v1/contacts/{contactId} - 인증 토큰 없이 호출 시 4xx")
    void deleteContact_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/contacts/1"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}