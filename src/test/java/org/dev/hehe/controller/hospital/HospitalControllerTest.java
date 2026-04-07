package org.dev.hehe.controller.hospital;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.hospital.HospitalClusterItem;
import org.dev.hehe.dto.hospital.HospitalDetailResponse;
import org.dev.hehe.dto.hospital.HospitalEquipmentInfo;
import org.dev.hehe.dto.hospital.HospitalListResponse;
import org.dev.hehe.dto.hospital.HospitalMapResponse;
import org.dev.hehe.service.hospital.HospitalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HospitalController 단위 테스트
 */
@WebMvcTest(HospitalController.class)
@DisplayName("HospitalController 테스트")
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HospitalService hospitalService;

    // =============================================
    // GET /api/v1/hospitals/map 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/hospitals/map - 클러스터 조회 성공")
    void getMapClusters_success() throws Exception {
        // given
        HospitalClusterItem item1 = createClusterItem(23, 37.52, 127.05);
        HospitalClusterItem item2 = createClusterItem(8, 37.48, 127.03);
        HospitalMapResponse response = HospitalMapResponse.builder()
                .precision(2)
                .items(List.of(item1, item2))
                .build();

        given(hospitalService.getMapClusters(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(12), isNull()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hospitals/map")
                        .param("swLat", "37.48").param("swLng", "126.98")
                        .param("neLat", "37.56").param("neLng", "127.08")
                        .param("zoomLevel", "12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.precision").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].count").value(23))
                .andExpect(jsonPath("$.data.items[0].lat").value(37.52))
                .andExpect(jsonPath("$.data.items[1].count").value(8));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/map - equipId 필터 적용")
    void getMapClusters_withEquipFilter() throws Exception {
        // given
        HospitalClusterItem item = createClusterItem(5, 37.52, 127.05);
        HospitalMapResponse response = HospitalMapResponse.builder()
                .precision(2)
                .items(List.of(item))
                .build();

        given(hospitalService.getMapClusters(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(12), eq(1L)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hospitals/map")
                        .param("swLat", "37.48").param("swLng", "126.98")
                        .param("neLat", "37.56").param("neLng", "127.08")
                        .param("zoomLevel", "12")
                        .param("equipId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].count").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/map - 필수 파라미터 누락 시 400")
    void getMapClusters_missingParam() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/map")
                        .param("swLat", "37.48"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("C002"));
    }

    // =============================================
    // GET /api/v1/hospitals 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/hospitals - 클러스터 병원 목록 조회 성공")
    void getHospitalsByCluster_success() throws Exception {
        // given
        List<HospitalListResponse> mockList = List.of(
                HospitalListResponse.builder()
                        .hospitalId(101L).name("강남 제모 클리닉")
                        .address("서울 강남구 역삼동 1").tags(List.of("여성원장", "주차가능"))
                        .build(),
                HospitalListResponse.builder()
                        .hospitalId(102L).name("역삼 스킨케어")
                        .address("서울 강남구 역삼동 2").tags(List.of())
                        .build()
        );

        given(hospitalService.getHospitalsByCluster(eq(37.52), eq(127.05), eq(2), isNull()))
                .willReturn(mockList);

        // when & then
        mockMvc.perform(get("/api/v1/hospitals")
                        .param("lat", "37.52").param("lng", "127.05").param("precision", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].hospitalId").value(101))
                .andExpect(jsonPath("$.data[0].name").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("여성원장"))
                .andExpect(jsonPath("$.data[1].tags").isArray())
                .andExpect(jsonPath("$.data[1].tags").isEmpty());
    }

    // =============================================
    // GET /api/v1/hospitals/{hospitalId} 테스트
    // =============================================

    @Test
    @DisplayName("GET /api/v1/hospitals/{hospitalId} - 병원 상세 조회 성공")
    void getHospitalDetail_success() throws Exception {
        // given
        HospitalEquipmentInfo equip = createEquipmentInfo("젠틀맥스프로", 2);
        HospitalDetailResponse response = HospitalDetailResponse.builder()
                .hospitalId(101L).name("강남 제모 클리닉")
                .address("서울 강남구 역삼동 1").lat(37.512).lng(127.059)
                .contactNumber("02-1234-5678").contactUrl(null)
                .tags(List.of("여성원장", "주차가능"))
                .equipments(List.of(equip))
                .build();

        given(hospitalService.getHospitalDetail(101L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/hospitals/101"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hospitalId").value(101))
                .andExpect(jsonPath("$.data.name").value("강남 제모 클리닉"))
                .andExpect(jsonPath("$.data.lat").value(37.512))
                .andExpect(jsonPath("$.data.contactUrl").doesNotExist())
                .andExpect(jsonPath("$.data.tags.length()").value(2))
                .andExpect(jsonPath("$.data.equipments[0].modelName").value("젠틀맥스프로"))
                .andExpect(jsonPath("$.data.equipments[0].totalCount").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/{hospitalId} - 존재하지 않는 병원 404")
    void getHospitalDetail_notFound() throws Exception {
        // given
        willThrow(new CommonException(ErrorCode.HOSPITAL_NOT_FOUND))
                .given(hospitalService).getHospitalDetail(999L);

        // when & then
        mockMvc.perform(get("/api/v1/hospitals/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("H001"));
    }

    // =============================================
    // 헬퍼 메서드
    // =============================================

    private HospitalClusterItem createClusterItem(int count, double lat, double lng) {
        HospitalClusterItem item = new HospitalClusterItem();
        ReflectionTestUtils.setField(item, "count", count);
        ReflectionTestUtils.setField(item, "lat", lat);
        ReflectionTestUtils.setField(item, "lng", lng);
        return item;
    }

    private HospitalEquipmentInfo createEquipmentInfo(String modelName, int totalCount) {
        HospitalEquipmentInfo info = new HospitalEquipmentInfo();
        ReflectionTestUtils.setField(info, "modelName", modelName);
        ReflectionTestUtils.setField(info, "totalCount", totalCount);
        return info;
    }
}