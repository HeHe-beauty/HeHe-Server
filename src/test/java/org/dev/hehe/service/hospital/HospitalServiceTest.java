package org.dev.hehe.service.hospital;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.hospital.HospitalDetail;
import org.dev.hehe.domain.hospital.HospitalSummary;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.dto.hospital.HospitalClusterItem;
import org.dev.hehe.dto.hospital.HospitalEquipmentInfo;
import org.dev.hehe.dto.hospital.HospitalListResponse;
import org.dev.hehe.dto.hospital.HospitalMapResponse;
import org.dev.hehe.mapper.hospital.HospitalMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * HospitalService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalService 테스트")
class HospitalServiceTest {

    @Mock
    private HospitalMapper hospitalMapper;

    @InjectMocks
    private HospitalService hospitalService;

    // =============================================
    // resolvePrecision 테스트
    // =============================================

    @Test
    @DisplayName("줌 레벨 1~9 → precision 1")
    void resolvePrecision_zoom1to9() {
        assertThat(hospitalService.resolvePrecision(1)).isEqualTo(1);
        assertThat(hospitalService.resolvePrecision(9)).isEqualTo(1);
    }

    @Test
    @DisplayName("줌 레벨 10~12 → precision 2")
    void resolvePrecision_zoom10to12() {
        assertThat(hospitalService.resolvePrecision(10)).isEqualTo(2);
        assertThat(hospitalService.resolvePrecision(12)).isEqualTo(2);
    }

    @Test
    @DisplayName("줌 레벨 13~14 → precision 3")
    void resolvePrecision_zoom13to14() {
        assertThat(hospitalService.resolvePrecision(13)).isEqualTo(3);
        assertThat(hospitalService.resolvePrecision(14)).isEqualTo(3);
    }

    @Test
    @DisplayName("줌 레벨 15 이상 → precision 4")
    void resolvePrecision_zoom15plus() {
        assertThat(hospitalService.resolvePrecision(15)).isEqualTo(4);
        assertThat(hospitalService.resolvePrecision(20)).isEqualTo(4);
    }

    // =============================================
    // getMapClusters 테스트
    // =============================================

    @Test
    @DisplayName("클러스터 조회 성공 - equipId 없음 (전체 병원)")
    void getMapClusters_success_noEquipFilter() {
        // given
        HospitalClusterItem cluster = createClusterItem(23, 37.52, 127.05);
        given(hospitalMapper.findClusters(any(), eq(2), isNull()))
                .willReturn(List.of(cluster));

        // when
        HospitalMapResponse response = hospitalService.getMapClusters(37.48, 126.98, 37.56, 127.08, 12, null);

        // then
        assertThat(response.getPrecision()).isEqualTo(2);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getCount()).isEqualTo(23);
    }

    @Test
    @DisplayName("클러스터 조회 성공 - equipId 있음 (장비 필터)")
    void getMapClusters_success_withEquipFilter() {
        // given
        HospitalClusterItem cluster = createClusterItem(5, 37.52, 127.05);
        given(hospitalMapper.findClusters(any(), eq(2), eq(1L)))
                .willReturn(List.of(cluster));

        // when
        HospitalMapResponse response = hospitalService.getMapClusters(37.48, 126.98, 37.56, 127.08, 12, 1L);

        // then
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("클러스터 조회 성공 - 뷰포트 내 병원 없음 (빈 리스트)")
    void getMapClusters_empty() {
        // given
        given(hospitalMapper.findClusters(any(), anyInt(), isNull())).willReturn(List.of());

        // when
        HospitalMapResponse response = hospitalService.getMapClusters(37.48, 126.98, 37.56, 127.08, 12, null);

        // then
        assertThat(response.getItems()).isEmpty();
    }

    // =============================================
    // getHospitalsByCluster 테스트
    // =============================================

    @Test
    @DisplayName("클러스터 병원 목록 조회 성공 - 태그 포함")
    void getHospitalsByCluster_success_withTags() {
        // given
        HospitalSummary summary1 = createSummary(101L, "강남 제모 클리닉", "서울 강남구 역삼동 1");
        HospitalSummary summary2 = createSummary(102L, "역삼 스킨케어", "서울 강남구 역삼동 2");

        HospitalTag tag1 = createTag(101L, "여성원장");
        HospitalTag tag2 = createTag(101L, "주차가능");
        HospitalTag tag3 = createTag(102L, "야간진료");

        given(hospitalMapper.findHospitalsByCluster(eq(37.52), eq(127.05), eq(2), isNull()))
                .willReturn(List.of(summary1, summary2));
        given(hospitalMapper.findTagsByHospitalIds(List.of(101L, 102L)))
                .willReturn(List.of(tag1, tag2, tag3));

        // when
        List<HospitalListResponse> result = hospitalService.getHospitalsByCluster(37.52, 127.05, 2, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHospitalId()).isEqualTo(101L);
        assertThat(result.get(0).getTags()).containsExactlyInAnyOrder("여성원장", "주차가능");
        assertThat(result.get(1).getTags()).containsExactly("야간진료");
    }

    @Test
    @DisplayName("클러스터 병원 목록 조회 성공 - 빈 클러스터")
    void getHospitalsByCluster_empty() {
        // given
        given(hospitalMapper.findHospitalsByCluster(anyDouble(), anyDouble(), anyInt(), isNull()))
                .willReturn(List.of());

        // when
        List<HospitalListResponse> result = hospitalService.getHospitalsByCluster(37.52, 127.05, 2, null);

        // then
        assertThat(result).isEmpty();
        verify(hospitalMapper).findHospitalsByCluster(anyDouble(), anyDouble(), anyInt(), isNull());
        verifyNoMoreInteractions(hospitalMapper);
    }

    // =============================================
    // getHospitalDetail 테스트
    // =============================================

    @Test
    @DisplayName("병원 상세 조회 성공 - 태그 및 장비 포함")
    void getHospitalDetail_success() {
        // given
        HospitalDetail detail = createDetail(101L, "강남 제모 클리닉", "서울 강남구", 37.512, 127.059, "02-1234-5678", null);
        HospitalEquipmentInfo equip = createEquipmentInfo("젠틀맥스프로", 2);

        given(hospitalMapper.findHospitalById(101L)).willReturn(Optional.of(detail));
        given(hospitalMapper.findTagNamesByHospitalId(101L)).willReturn(List.of("여성원장", "주차가능"));
        given(hospitalMapper.findEquipmentsByHospitalId(101L)).willReturn(List.of(equip));

        // when
        var response = hospitalService.getHospitalDetail(101L);

        // then
        assertThat(response.getHospitalId()).isEqualTo(101L);
        assertThat(response.getName()).isEqualTo("강남 제모 클리닉");
        assertThat(response.getLat()).isEqualTo(37.512);
        assertThat(response.getTags()).containsExactlyInAnyOrder("여성원장", "주차가능");
        assertThat(response.getEquipments()).hasSize(1);
        assertThat(response.getEquipments().get(0).getModelName()).isEqualTo("젠틀맥스프로");
    }

    @Test
    @DisplayName("병원 상세 조회 실패 - 존재하지 않는 hospitalId → H001")
    void getHospitalDetail_notFound() {
        // given
        given(hospitalMapper.findHospitalById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> hospitalService.getHospitalDetail(999L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.HOSPITAL_NOT_FOUND));

        verify(hospitalMapper).findHospitalById(999L);
        verifyNoMoreInteractions(hospitalMapper);
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

    private HospitalSummary createSummary(Long hospitalId, String name, String address) {
        HospitalSummary summary = new HospitalSummary();
        ReflectionTestUtils.setField(summary, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(summary, "name", name);
        ReflectionTestUtils.setField(summary, "address", address);
        return summary;
    }

    private HospitalTag createTag(Long hospitalId, String tagName) {
        HospitalTag tag = new HospitalTag();
        ReflectionTestUtils.setField(tag, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(tag, "tagName", tagName);
        return tag;
    }

    private HospitalDetail createDetail(Long hospitalId, String name, String address,
                                        double lat, double lng,
                                        String contactNumber, String contactUrl) {
        HospitalDetail detail = new HospitalDetail();
        ReflectionTestUtils.setField(detail, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(detail, "name", name);
        ReflectionTestUtils.setField(detail, "address", address);
        ReflectionTestUtils.setField(detail, "lat", lat);
        ReflectionTestUtils.setField(detail, "lng", lng);
        ReflectionTestUtils.setField(detail, "contactNumber", contactNumber);
        ReflectionTestUtils.setField(detail, "contactUrl", contactUrl);
        return detail;
    }

    private HospitalEquipmentInfo createEquipmentInfo(String modelName, int totalCount) {
        HospitalEquipmentInfo info = new HospitalEquipmentInfo();
        ReflectionTestUtils.setField(info, "modelName", modelName);
        ReflectionTestUtils.setField(info, "totalCount", totalCount);
        return info;
    }
}