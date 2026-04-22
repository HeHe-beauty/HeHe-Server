package org.dev.hehe.service.recentview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.domain.recentview.RecentView;
import org.dev.hehe.dto.recentview.RecentViewResponse;
import org.dev.hehe.mapper.hospital.HospitalMapper;
import org.dev.hehe.mapper.recentview.RecentViewMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 최근 본 병원 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecentViewService {

    private final RecentViewMapper recentViewMapper;
    private final HospitalMapper hospitalMapper;

    /**
     * 최근 본 병원 목록 조회 (최신 순 10건)
     *
     * <p>N+1 방지를 위해 태그를 IN 절로 일괄 조회한다.</p>
     *
     * @param userId JWT에서 추출한 유저 ID
     * @return 최근 본 병원 목록 (최대 10건)
     */
    public List<RecentViewResponse> getRecentViews(Long userId) {
        log.info("최근 본 병원 조회 - userId={}", userId);

        List<RecentView> recentViews = recentViewMapper.findRecentViews(userId);

        if (recentViews.isEmpty()) {
            return List.of();
        }

        List<Long> hospitalIds = recentViews.stream()
                .map(RecentView::getHospitalId)
                .toList();

        Map<Long, List<String>> tagsMap = hospitalMapper.findTagsByHospitalIds(hospitalIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getHospitalId(),
                        Collectors.mapping(tag -> tag.getTagName(), Collectors.toList())
                ));

        List<RecentViewResponse> result = recentViews.stream()
                .map(rv -> RecentViewResponse.of(rv, tagsMap.getOrDefault(rv.getHospitalId(), List.of())))
                .toList();

        log.info("최근 본 병원 조회 완료 - userId={}, count={}", userId, result.size());
        return result;
    }

    /**
     * 최근 본 병원 기록 (upsert)
     *
     * <p>이미 기록된 병원이면 viewed_at을 현재 시각으로 갱신한다.</p>
     *
     * @param userId     JWT에서 추출한 유저 ID
     * @param hospitalId 병원 ID
     */
    public void recordRecentView(Long userId, Long hospitalId) {
        log.info("최근 본 병원 기록 - userId={}, hospitalId={}", userId, hospitalId);
        recentViewMapper.upsertRecentView(userId, hospitalId);
    }
}