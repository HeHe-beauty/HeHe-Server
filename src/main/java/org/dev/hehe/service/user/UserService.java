package org.dev.hehe.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.user.User;
import org.dev.hehe.dto.user.UserAgreementsRequest;
import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.dto.user.UserWithdrawRequest;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.contact.ContactMapper;
import org.dev.hehe.mapper.pushtoken.PushTokenMapper;
import org.dev.hehe.mapper.recentview.RecentViewMapper;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.dev.hehe.mapper.user.UserMapper;
import org.dev.hehe.service.auth.RedisTokenService;
import org.dev.hehe.service.auth.oauth.KakaoOAuthClient;
import org.dev.hehe.service.auth.oauth.NaverOAuthClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
    private final RecentViewMapper recentViewMapper;
    private final PushTokenMapper pushTokenMapper;
    private final UserMapper userMapper;
    private final RedisTokenService redisTokenService;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final NaverOAuthClient naverOAuthClient;

    private static final String UNLINK_STATUS_SUCCESS = "SUCCESS";
    private static final String UNLINK_STATUS_FAILED = "FAILED";

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

    /**
     * 회원 탈퇴 (소프트 삭제)
     *
     * <p>status를 LEAVE로 전환하고 로그인 토큰을 무효화한다. 찜·일정·최근 본 병원·문의 내역 등
     * 연관 데이터는 즉시 삭제하지 않고 하드 삭제 배치({@link #purgeUser})가 일정 기간 후 정리한다.</p>
     *
     * <p>request에 provider/providerAccessToken이 있으면 소셜 unlink를 best-effort로 시도한다.
     * unlink 실패해도 탈퇴 자체는 그대로 진행하며, 결과만 unlink_status에 기록한다.</p>
     *
     * @param userId  JWT에서 추출한 유저 ID
     * @param request 소셜 unlink용 provider/accessToken (선택)
     * @throws CommonException U001 (유저를 찾을 수 없음)
     */
    public void deleteAccount(Long userId, UserWithdrawRequest request) {
        User user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        if ("LEAVE".equals(user.getStatus())) {
            log.info("이미 탈퇴 처리된 유저 - userId={}", userId);
            return;
        }

        String unlinkStatus = tryUnlink(userId, request);

        userMapper.withdrawUser(userId, unlinkStatus);
        redisTokenService.delete(userId);
        pushTokenMapper.deactivateAllByUserId(userId);

        log.info("회원 탈퇴 처리 완료 - userId={}, unlinkStatus={}", userId, unlinkStatus);
    }

    /**
     * 소셜 unlink 시도 (best-effort)
     *
     * @return SUCCESS/FAILED, provider/token 미제공 시 null(미시도)
     */
    private String tryUnlink(Long userId, UserWithdrawRequest request) {
        if (request == null || request.getProvider() == null || request.getProviderAccessToken() == null
                || request.getProviderAccessToken().isBlank()) {
            return null;
        }

        boolean success = switch (request.getProvider().toUpperCase()) {
            case "KAKAO" -> kakaoOAuthClient.unlink(request.getProviderAccessToken());
            case "NAVER" -> naverOAuthClient.unlink(request.getProviderAccessToken());
            default -> {
                log.warn("[Withdraw] 알 수 없는 provider - userId={}, provider={}", userId, request.getProvider());
                yield false;
            }
        };

        return success ? UNLINK_STATUS_SUCCESS : UNLINK_STATUS_FAILED;
    }

    /**
     * 알림 동의 여부 변경 (부분 수정 — null인 필드는 기존값 유지)
     *
     * @param userId  JWT에서 추출한 유저 ID
     * @param request 변경할 동의값 (push/night/mkt, 각각 선택)
     */
    public void updateAgreements(Long userId, UserAgreementsRequest request) {
        userMapper.updateAgreements(userId, request.getPushAgreed(), request.getNightAgreed(), request.getMktAgreed());
        log.info("알림 동의 변경 완료 - userId={}, pushAgreed={}, nightAgreed={}, mktAgreed={}",
                userId, request.getPushAgreed(), request.getNightAgreed(), request.getMktAgreed());
    }

    /**
     * 하드 삭제 배치 대상 유저 ID 조회
     *
     * @return 탈퇴 후 보관 기간이 지난 유저 ID 목록
     */
    public List<Long> findLeaveUserIdsForPurge() {
        return userMapper.findLeaveUserIdsForPurge();
    }

    /**
     * 탈퇴 유저 물리 삭제 (하드 삭제 배치 전용)
     *
     * <p>연관 데이터를 모두 삭제한 뒤 유저 row 자체를 삭제한다. 스케줄러(다른 빈)에서 호출되어야
     * {@code @Transactional}이 프록시를 거쳐 정상 적용된다 (self-invocation 주의).</p>
     *
     * @param userId 물리 삭제할 유저 ID
     */
    @Transactional
    public void purgeUser(Long userId) {
        bookmarkMapper.deleteAllByUserId(userId);
        scheduleMapper.deleteAllAlarmsByUserId(userId);
        scheduleMapper.deleteAllSchedulesByUserId(userId);
        recentViewMapper.deleteAllByUserId(userId);
        contactMapper.deleteAllByUserId(userId);
        pushTokenMapper.deleteAllByUserId(userId);
        userMapper.deleteByUserId(userId);

        log.info("탈퇴 유저 물리 삭제 완료 - userId={}", userId);
    }
}