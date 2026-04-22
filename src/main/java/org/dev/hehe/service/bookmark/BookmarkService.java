package org.dev.hehe.service.bookmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.domain.bookmark.BookmarkedHospital;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.bookmark.BookmarkResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.hospital.HospitalMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 찜(북마크) 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkMapper bookmarkMapper;
    private final HospitalMapper hospitalMapper;

    /**
     * 유저의 찜한 병원 목록 조회
     *
     * <p>찜한 최신 순으로 정렬하여 반환한다.</p>
     * <p>태그는 IN 절로 일괄 조회하여 N+1 방지.</p>
     *
     * @param userId 조회할 유저 ID
     * @return 찜한 병원 목록 (태그 포함, 찜한 최신 순)
     */
    public List<BookmarkResponse> getBookmarks(Long userId) {
        log.debug("찜 목록 조회 - userId={}", userId);

        List<BookmarkedHospital> hospitals = bookmarkMapper.findBookmarkedHospitals(userId);

        if (hospitals.isEmpty()) {
            log.debug("찜 목록 없음 - userId={}", userId);
            return List.of();
        }

        List<Long> hospitalIds = hospitals.stream()
                .map(BookmarkedHospital::getHospitalId)
                .toList();

        // 태그 일괄 조회 (N+1 방지)
        Map<Long, List<String>> tagMap = hospitalMapper.findTagsByHospitalIds(hospitalIds)
                .stream()
                .collect(Collectors.groupingBy(
                        HospitalTag::getHospitalId,
                        Collectors.mapping(HospitalTag::getTagName, Collectors.toList())
                ));

        log.debug("찜 목록 조회 완료 - userId={}, count={}", userId, hospitals.size());

        return hospitals.stream()
                .map(h -> BookmarkResponse.of(h, tagMap.getOrDefault(h.getHospitalId(), List.of())))
                .toList();
    }

    /**
     * 병원 찜 추가
     *
     * @param userId     찜하는 유저 ID
     * @param hospitalId 찜할 병원 ID
     * @throws CommonException BOOKMARK_ALREADY_EXISTS — 이미 찜한 병원인 경우
     */
    public void addBookmark(Long userId, Long hospitalId) {
        log.info("찜 추가 - userId={}, hospitalId={}", userId, hospitalId);

        if (bookmarkMapper.existsBookmark(userId, hospitalId)) {
            log.warn("이미 찜한 병원 - userId={}, hospitalId={}", userId, hospitalId);
            throw new CommonException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        bookmarkMapper.insertBookmark(userId, hospitalId);
        log.info("찜 추가 완료 - userId={}, hospitalId={}", userId, hospitalId);
    }

    /**
     * 병원 찜 삭제
     *
     * @param userId     유저 ID
     * @param hospitalId 삭제할 병원 ID
     * @throws CommonException BOOKMARK_NOT_FOUND — 찜하지 않은 병원인 경우
     */
    public void removeBookmark(Long userId, Long hospitalId) {
        log.info("찜 삭제 - userId={}, hospitalId={}", userId, hospitalId);

        if (!bookmarkMapper.existsBookmark(userId, hospitalId)) {
            log.warn("찜하지 않은 병원 - userId={}, hospitalId={}", userId, hospitalId);
            throw new CommonException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        bookmarkMapper.deleteBookmark(userId, hospitalId);
        log.info("찜 삭제 완료 - userId={}, hospitalId={}", userId, hospitalId);
    }
}