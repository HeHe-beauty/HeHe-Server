package org.dev.hehe.service.hospital;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.hospital.HospitalSummary;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.dto.hospital.HospitalClusterItem;
import org.dev.hehe.dto.hospital.HospitalDetailResponse;
import org.dev.hehe.dto.hospital.HospitalEquipmentInfo;
import org.dev.hehe.dto.hospital.HospitalListResponse;
import org.dev.hehe.dto.hospital.HospitalMapResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.hospital.HospitalMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 병원 지도 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalMapper hospitalMapper;
    private final BookmarkMapper bookmarkMapper;

    /**
     * 지도 뷰포트 내 클러스터 목록 조회
     *
     * <p>줌 레벨로 precision(좌표 반올림 자릿수)을 산출한 뒤,
     * MBRContains 공간 쿼리로 뷰포트 내 병원을 필터링하고 좌표 기준 GROUP BY 집계한다.</p>
     *
     * @param swLat     뷰포트 남서 위도
     * @param swLng     뷰포트 남서 경도
     * @param neLat     뷰포트 북동 위도
     * @param neLng     뷰포트 북동 경도
     * @param zoomLevel 현재 지도 줌 레벨
     * @param equipId   장비 필터 (null 이면 전체 병원)
     * @return 클러스터 목록 및 precision
     */
    public HospitalMapResponse getMapClusters(double swLat, double swLng,
                                              double neLat, double neLng,
                                              int zoomLevel, Long equipId) {
        int precision = resolvePrecision(zoomLevel);
        String viewport = buildViewportPolygon(swLng, swLat, neLng, neLat);

        log.debug("클러스터 조회 - zoomLevel={}, precision={}, equipId={}", zoomLevel, precision, equipId);

        List<HospitalClusterItem> items = hospitalMapper.findClusters(viewport, precision, equipId);

        log.debug("클러스터 조회 완료 - count={}", items.size());
        return HospitalMapResponse.builder()
                .precision(precision)
                .items(items)
                .build();
    }

    /**
     * 클러스터 내 병원 목록 조회
     *
     * <p>클릭한 클러스터의 lat, lng, precision 으로 동일 좌표 그룹에 속한 병원 목록을 반환한다.</p>
     * <p>태그는 IN 절로 일괄 조회하여 N+1 방지.</p>
     *
     * @param lat       클러스터 반올림 위도 (map API 응답값 그대로)
     * @param lng       클러스터 반올림 경도 (map API 응답값 그대로)
     * @param precision 좌표 반올림 자릿수 (map API 응답값 그대로)
     * @param equipId   장비 필터 (null 이면 전체)
     * @return 병원 목록 (태그 포함)
     */
    public List<HospitalListResponse> getHospitalsByCluster(double lat, double lng,
                                                            int precision, Long equipId,
                                                            Long userId) {
        log.debug("클러스터 병원 목록 조회 - lat={}, lng={}, precision={}, equipId={}", lat, lng, precision, equipId);

        List<HospitalSummary> summaries = hospitalMapper.findHospitalsByCluster(lat, lng, precision, equipId);

        if (summaries.isEmpty()) {
            return List.of();
        }

        List<Long> hospitalIds = summaries.stream()
                .map(HospitalSummary::getHospitalId)
                .toList();

        // 태그 일괄 조회 (N+1 방지)
        Map<Long, List<String>> tagMap = hospitalMapper.findTagsByHospitalIds(hospitalIds)
                .stream()
                .collect(Collectors.groupingBy(
                        HospitalTag::getHospitalId,
                        Collectors.mapping(HospitalTag::getTagName, Collectors.toList())
                ));

        // 찜 여부 일괄 조회 (로그인 시에만)
        Set<Long> bookmarkedIds = userId != null
                ? new HashSet<>(bookmarkMapper.findBookmarkedHospitalIds(userId, hospitalIds))
                : Set.of();

        log.debug("클러스터 병원 목록 조회 완료 - hospitalCount={}", summaries.size());

        return summaries.stream()
                .map(s -> HospitalListResponse.of(
                        s,
                        tagMap.getOrDefault(s.getHospitalId(), List.of()),
                        userId != null ? bookmarkedIds.contains(s.getHospitalId()) : null
                ))
                .toList();
    }

    /**
     * 병원 상세 단건 조회
     *
     * @param hospitalId 조회할 병원 ID
     * @return 병원 상세 응답 (태그, 장비 포함)
     * @throws CommonException HOSPITAL_NOT_FOUND — 존재하지 않는 hospitalId
     */
    public HospitalDetailResponse getHospitalDetail(Long hospitalId, Long userId) {
        log.debug("병원 상세 조회 - hospitalId={}", hospitalId);

        var detail = hospitalMapper.findHospitalById(hospitalId)
                .orElseThrow(() -> {
                    log.warn("병원을 찾을 수 없음 - hospitalId={}", hospitalId);
                    return new CommonException(ErrorCode.HOSPITAL_NOT_FOUND);
                });

        List<String> tags = hospitalMapper.findTagNamesByHospitalId(hospitalId);
        List<HospitalEquipmentInfo> equipments = hospitalMapper.findEquipmentsByHospitalId(hospitalId);

        // 찜 여부 조회 (로그인 시에만)
        Boolean isBookmarked = userId != null ? bookmarkMapper.existsBookmark(userId, hospitalId) : null;

        log.debug("병원 상세 조회 완료 - hospitalId={}, tagCount={}, equipCount={}",
                hospitalId, tags.size(), equipments.size());

        return HospitalDetailResponse.of(detail, tags, equipments, isBookmarked);
    }

    /**
     * 줌 레벨 → 좌표 반올림 자릿수 변환
     *
     * <pre>
     * zoomLevel  1~ 9  → precision 1 (~11km)
     * zoomLevel 10~12  → precision 2 (~1km)
     * zoomLevel 13~14  → precision 3 (~100m)
     * zoomLevel 15~    → precision 4 (~10m)
     * </pre>
     *
     * @param zoomLevel 지도 줌 레벨
     * @return 좌표 반올림 자릿수
     */
    int resolvePrecision(int zoomLevel) {
        if (zoomLevel <= 9)  return 1;
        if (zoomLevel <= 12) return 2;
        if (zoomLevel <= 14) return 3;
        return 4;
    }

    /**
     * 뷰포트 좌표로 MBRContains 에 사용할 WKT Polygon 문자열 생성
     *
     * @param swLng 남서 경도
     * @param swLat 남서 위도
     * @param neLng 북동 경도
     * @param neLat 북동 위도
     * @return WKT Polygon 문자열
     */
    private String buildViewportPolygon(double swLng, double swLat, double neLng, double neLat) {
        return String.format(Locale.US,
                "POLYGON((%f %f,%f %f,%f %f,%f %f,%f %f))",
                swLat, swLng,
                swLat, neLng,
                neLat, neLng,
                neLat, swLng,
                swLat, swLng
        );
    }
}