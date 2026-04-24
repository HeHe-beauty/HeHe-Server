package org.dev.hehe.controller.hospital;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.hospital.HospitalClusterRequest;
import org.dev.hehe.dto.hospital.HospitalDetailResponse;
import org.dev.hehe.dto.hospital.HospitalListResponse;
import org.dev.hehe.dto.hospital.HospitalMapRequest;
import org.dev.hehe.dto.hospital.HospitalMapResponse;
import org.dev.hehe.service.hospital.HospitalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 병원 지도 컨트롤러
 * Swagger 명세는 HospitalApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController implements HospitalApiSpecification {

    private final HospitalService hospitalService;

    /** 지도 뷰포트 내 클러스터 목록 조회 */
    @Override
    @GetMapping("/map")
    public ApiResult<HospitalMapResponse> getMapClusters(@Valid HospitalMapRequest request) {
        log.info("[GET] /api/v1/hospitals/map - zoomLevel={}, equipId={}", request.getZoomLevel(), request.getEquipId());
        HospitalMapResponse response = hospitalService.getMapClusters(
                request.getSwLat(), request.getSwLng(),
                request.getNeLat(), request.getNeLng(),
                request.getZoomLevel(), request.getEquipId());
        log.info("클러스터 조회 완료 - precision={}, itemCount={}", response.getPrecision(), response.getItems().size());
        return ApiResult.ok(response);
    }

    /** 클러스터 내 병원 목록 조회 */
    @Override
    @GetMapping
    public ApiResult<List<HospitalListResponse>> getHospitalsByCluster(@Valid HospitalClusterRequest request,
                                                                        @LoginUser Long userId) {
        log.info("[GET] /api/v1/hospitals - lat={}, lng={}, precision={}, equipId={}",
                request.getLat(), request.getLng(), request.getPrecision(), request.getEquipId());
        List<HospitalListResponse> response = hospitalService.getHospitalsByCluster(
                request.getLat(), request.getLng(), request.getPrecision(), request.getEquipId(), userId);
        log.info("병원 목록 조회 완료 - count={}", response.size());
        return ApiResult.ok(response);
    }

    /** 병원 상세 조회 */
    @Override
    @GetMapping("/{hospitalId}")
    public ApiResult<HospitalDetailResponse> getHospitalDetail(@PathVariable Long hospitalId,
                                                                @LoginUser Long userId) {
        log.info("[GET] /api/v1/hospitals/{} - 병원 상세 조회", hospitalId);
        HospitalDetailResponse response = hospitalService.getHospitalDetail(hospitalId, userId);
        log.info("병원 상세 조회 완료 - hospitalId={}", hospitalId);
        return ApiResult.ok(response);
    }
}