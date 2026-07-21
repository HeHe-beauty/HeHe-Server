package org.dev.hehe.service.user;

import org.dev.hehe.common.exception.CommonException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private BookmarkMapper bookmarkMapper;

    @Mock
    private ContactMapper contactMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private RecentViewMapper recentViewMapper;

    @Mock
    private PushTokenMapper pushTokenMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTokenService redisTokenService;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private NaverOAuthClient naverOAuthClient;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("마이페이지 요약 조회 성공 - 이메일 제공 동의한 유저")
    void getSummary_success() {
        // given
        User user = createUser("ACTIVE");
        ReflectionTestUtils.setField(user, "email", "hong@example.com");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(bookmarkMapper.countBookmarks(1L)).willReturn(5);
        given(contactMapper.countContacts(1L)).willReturn(3);
        given(scheduleMapper.countUpcomingSchedules(eq(1L), anyLong())).willReturn(2);

        // when
        UserSummaryResponse result = userService.getSummary(1L);

        // then
        assertThat(result.getEmail()).isEqualTo("hong@example.com");
        assertThat(result.getBookmarkCount()).isEqualTo(5);
        assertThat(result.getContactCount()).isEqualTo(3);
        assertThat(result.getScheduleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("마이페이지 요약 조회 - 모든 항목 0건, 이메일 미제공 시 null")
    void getSummary_allZero() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(bookmarkMapper.countBookmarks(1L)).willReturn(0);
        given(contactMapper.countContacts(1L)).willReturn(0);
        given(scheduleMapper.countUpcomingSchedules(eq(1L), anyLong())).willReturn(0);

        // when
        UserSummaryResponse result = userService.getSummary(1L);

        // then
        assertThat(result.getEmail()).isNull();
        assertThat(result.getBookmarkCount()).isZero();
        assertThat(result.getContactCount()).isZero();
        assertThat(result.getScheduleCount()).isZero();
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - unlink 요청 없으면 unlink 미시도, status LEAVE 전환/토큰/푸시 정리")
    void withdraw_noUnlinkRequest_success() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        userService.deleteAccount(1L, null);

        // then
        verify(userMapper, times(1)).withdrawUser(1L, null);
        verify(kakaoOAuthClient, never()).unlink(anyString());
        verify(redisTokenService, times(1)).delete(1L);
        verify(pushTokenMapper, times(1)).deactivateAllByUserId(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 - 카카오 unlink 성공 시 SUCCESS 기록")
    void withdraw_kakaoUnlinkSuccess() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(kakaoOAuthClient.unlink("kakao-token")).willReturn(true);

        // when
        userService.deleteAccount(1L, createWithdrawRequest("KAKAO", "kakao-token"));

        // then
        verify(userMapper, times(1)).withdrawUser(1L, "SUCCESS");
    }

    @Test
    @DisplayName("회원 탈퇴 - 카카오 unlink 실패해도 탈퇴는 그대로 진행되고 FAILED 기록")
    void withdraw_kakaoUnlinkFailed_stillWithdraws() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(kakaoOAuthClient.unlink("kakao-token")).willReturn(false);

        // when
        userService.deleteAccount(1L, createWithdrawRequest("KAKAO", "kakao-token"));

        // then
        verify(userMapper, times(1)).withdrawUser(1L, "FAILED");
        verify(redisTokenService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 - 네이버 unlink 성공 시 SUCCESS 기록, 카카오 클라이언트는 호출 안 함")
    void withdraw_naverUnlinkSuccess() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(naverOAuthClient.unlink("naver-token")).willReturn(true);

        // when
        userService.deleteAccount(1L, createWithdrawRequest("NAVER", "naver-token"));

        // then
        verify(userMapper, times(1)).withdrawUser(1L, "SUCCESS");
        verify(kakaoOAuthClient, never()).unlink(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴 - 네이버 unlink 실패해도 탈퇴는 그대로 진행되고 FAILED 기록")
    void withdraw_naverUnlinkFailed_stillWithdraws() {
        // given
        User user = createUser("ACTIVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));
        given(naverOAuthClient.unlink("naver-token")).willReturn(false);

        // when
        userService.deleteAccount(1L, createWithdrawRequest("NAVER", "naver-token"));

        // then
        verify(userMapper, times(1)).withdrawUser(1L, "FAILED");
    }

    @Test
    @DisplayName("회원 탈퇴 - 이미 탈퇴한 유저는 idempotent하게 아무 작업도 하지 않는다")
    void withdraw_alreadyLeave_noop() {
        // given
        User user = createUser("LEAVE");
        given(userMapper.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        userService.deleteAccount(1L, null);

        // then
        verify(userMapper, never()).withdrawUser(anyLong(), anyString());
        verify(redisTokenService, never()).delete(1L);
        verify(pushTokenMapper, never()).deactivateAllByUserId(1L);
    }

    @Test
    @DisplayName("회원 탈퇴 - 존재하지 않는 유저면 U001 예외")
    void withdraw_userNotFound_throwsException() {
        // given
        given(userMapper.findByUserId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteAccount(1L, null))
                .isInstanceOf(CommonException.class);
    }

    @Test
    @DisplayName("탈퇴 유저 물리 삭제 - 연관 데이터 전체 삭제 후 유저 삭제")
    void purgeUser_success() {
        // when
        userService.purgeUser(1L);

        // then
        verify(bookmarkMapper, times(1)).deleteAllByUserId(1L);
        verify(scheduleMapper, times(1)).deleteAllAlarmsByUserId(1L);
        verify(scheduleMapper, times(1)).deleteAllSchedulesByUserId(1L);
        verify(recentViewMapper, times(1)).deleteAllByUserId(1L);
        verify(contactMapper, times(1)).deleteAllByUserId(1L);
        verify(pushTokenMapper, times(1)).deleteAllByUserId(1L);
        verify(userMapper, times(1)).deleteByUserId(1L);
    }

    @Test
    @DisplayName("알림 동의 변경 - 일부 필드만 전달해도 매퍼로 그대로 전달")
    void updateAgreements_partial_success() {
        // given
        UserAgreementsRequest request = createAgreementsRequest(true, null, false);

        // when
        userService.updateAgreements(1L, request);

        // then
        verify(userMapper, times(1)).updateAgreements(1L, true, null, false);
    }

    private User createUser(String status) {
        User user = new User();
        ReflectionTestUtils.setField(user, "userId", 1L);
        ReflectionTestUtils.setField(user, "status", status);
        return user;
    }

    private UserWithdrawRequest createWithdrawRequest(String provider, String providerAccessToken) {
        UserWithdrawRequest request = new UserWithdrawRequest();
        ReflectionTestUtils.setField(request, "provider", provider);
        ReflectionTestUtils.setField(request, "providerAccessToken", providerAccessToken);
        return request;
    }

    private UserAgreementsRequest createAgreementsRequest(Boolean pushAgreed, Boolean nightAgreed, Boolean mktAgreed) {
        UserAgreementsRequest request = new UserAgreementsRequest();
        ReflectionTestUtils.setField(request, "pushAgreed", pushAgreed);
        ReflectionTestUtils.setField(request, "nightAgreed", nightAgreed);
        ReflectionTestUtils.setField(request, "mktAgreed", mktAgreed);
        return request;
    }
}
