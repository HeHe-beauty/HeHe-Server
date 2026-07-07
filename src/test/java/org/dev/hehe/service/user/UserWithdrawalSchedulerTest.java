package org.dev.hehe.service.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * UserWithdrawalScheduler 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserWithdrawalScheduler 테스트")
class UserWithdrawalSchedulerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserWithdrawalScheduler userWithdrawalScheduler;

    @Test
    @DisplayName("삭제 대상이 없으면 아무 작업도 하지 않는다")
    void purgeWithdrawnUsers_noTargets_doesNothing() {
        given(userService.findLeaveUserIdsForPurge()).willReturn(List.of());

        userWithdrawalScheduler.purgeWithdrawnUsers();

        verify(userService, never()).purgeUser(anyLong());
    }

    @Test
    @DisplayName("삭제 대상 유저마다 purgeUser를 호출한다")
    void purgeWithdrawnUsers_hasTargets_purgesEach() {
        given(userService.findLeaveUserIdsForPurge()).willReturn(List.of(1L, 2L, 3L));

        userWithdrawalScheduler.purgeWithdrawnUsers();

        verify(userService, times(1)).purgeUser(1L);
        verify(userService, times(1)).purgeUser(2L);
        verify(userService, times(1)).purgeUser(3L);
    }
}
