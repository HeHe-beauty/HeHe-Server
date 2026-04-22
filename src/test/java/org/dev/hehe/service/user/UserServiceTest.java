package org.dev.hehe.service.user;

import org.dev.hehe.dto.user.UserSummaryResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.contact.ContactMapper;
import org.dev.hehe.mapper.schedule.ScheduleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("마이페이지 요약 조회 성공")
    void getSummary_success() {
        // given
        given(bookmarkMapper.countBookmarks(1L)).willReturn(5);
        given(contactMapper.countContacts(1L)).willReturn(3);
        given(scheduleMapper.countSchedules(1L)).willReturn(2);

        // when
        UserSummaryResponse result = userService.getSummary(1L);

        // then
        assertThat(result.getBookmarkCount()).isEqualTo(5);
        assertThat(result.getContactCount()).isEqualTo(3);
        assertThat(result.getScheduleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("마이페이지 요약 조회 - 모든 항목 0건")
    void getSummary_allZero() {
        // given
        given(bookmarkMapper.countBookmarks(1L)).willReturn(0);
        given(contactMapper.countContacts(1L)).willReturn(0);
        given(scheduleMapper.countSchedules(1L)).willReturn(0);

        // when
        UserSummaryResponse result = userService.getSummary(1L);

        // then
        assertThat(result.getBookmarkCount()).isZero();
        assertThat(result.getContactCount()).isZero();
        assertThat(result.getScheduleCount()).isZero();
    }
}
