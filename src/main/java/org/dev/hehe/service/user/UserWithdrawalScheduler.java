package org.dev.hehe.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 탈퇴 유저 하드 삭제 배치 스케줄러
 * 매일 새벽 4시 실행하여 보관 기간이 지난 탈퇴(LEAVE) 유저를 물리 삭제한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalScheduler {

    private final UserService userService;

    /**
     * 보관 기간 경과 탈퇴 유저 물리 삭제
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void purgeWithdrawnUsers() {
        List<Long> userIds = userService.findLeaveUserIdsForPurge();
        if (userIds.isEmpty()) {
            return;
        }

        log.info("탈퇴 유저 하드 삭제 배치 실행: 대상 {}건", userIds.size());

        for (Long userId : userIds) {
            userService.purgeUser(userId);
        }
    }
}
