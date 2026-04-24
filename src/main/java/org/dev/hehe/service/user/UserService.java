package org.dev.hehe.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.contact.ContactMapper;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 유저 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final BookmarkMapper bookmarkMapper;
    private final ContactMapper contactMapper;
    private final ScheduleMapper scheduleMapper;

    /**
     * 마이페이지 요약 정보 조회
     *
     * <p>찜 수, 문의 수(삭제 제외), 예정 예약 수(현재 시각 이후)를 한 번에 반환한다.</p>
     *
     * @param userId JWT에서 추출한 유저 ID
     * @return 찜 수, 문의 수, 예정 예약 수
     */
    public UserSummaryResponse getSummary(Long userId) {
        log.info("마이페이지 요약 조회 - userId={}", userId);

        int bookmarkCount = bookmarkMapper.countBookmarks(userId);
        int contactCount = contactMapper.countContacts(userId);
        int scheduleCount = scheduleMapper.countUpcomingSchedules(userId, Instant.now().getEpochSecond());

        log.info("마이페이지 요약 조회 완료 - userId={}, bookmark={}, contact={}, schedule={}",
                userId, bookmarkCount, contactCount, scheduleCount);

        return UserSummaryResponse.builder()
                .bookmarkCount(bookmarkCount)
                .contactCount(contactCount)
                .scheduleCount(scheduleCount)
                .build();
    }
}