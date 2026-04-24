package org.dev.hehe.service.recentview;

import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.domain.recentview.RecentView;
import org.dev.hehe.dto.recentview.RecentViewResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.hospital.HospitalMapper;
import org.dev.hehe.mapper.recentview.RecentViewMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * RecentViewService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecentViewService 테스트")
class RecentViewServiceTest {

    @Mock
    private RecentViewMapper recentViewMapper;

    @Mock
    private HospitalMapper hospitalMapper;

    @Mock
    private BookmarkMapper bookmarkMapper;

    @InjectMocks
    private RecentViewService recentViewService;

    @Test
    @DisplayName("최근 본 병원 목록 조회 성공")
    void getRecentViews_success() {
        // given
        RecentView rv = createRecentView(1L, 101L, "강남 제모 클리닉", "서울 강남구",
                LocalDateTime.of(2026, 4, 22, 10, 30));

        HospitalTag tag = new HospitalTag();
        ReflectionTestUtils.setField(tag, "hospitalId", 101L);
        ReflectionTestUtils.setField(tag, "tagName", "젠틀맥스프로");

        given(recentViewMapper.findRecentViews(1L)).willReturn(List.of(rv));
        given(hospitalMapper.findTagsByHospitalIds(List.of(101L))).willReturn(List.of(tag));
        given(bookmarkMapper.findBookmarkedHospitalIds(1L, List.of(101L))).willReturn(List.of(101L));

        // when
        List<RecentViewResponse> result = recentViewService.getRecentViews(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHospitalId()).isEqualTo(101L);
        assertThat(result.get(0).getName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.get(0).getTags()).containsExactly("젠틀맥스프로");
        assertThat(result.get(0).getIsBookmarked()).isTrue();
    }

    @Test
    @DisplayName("최근 본 병원 없음 - 빈 리스트 반환")
    void getRecentViews_empty() {
        // given
        given(recentViewMapper.findRecentViews(1L)).willReturn(List.of());

        // when
        List<RecentViewResponse> result = recentViewService.getRecentViews(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("최근 본 병원 기록 성공 (upsert)")
    void recordRecentView_success() {
        // when
        recentViewService.recordRecentView(1L, 101L);

        // then
        verify(recentViewMapper, times(1)).upsertRecentView(1L, 101L);
    }

    // =============================================
    // 헬퍼 메서드
    // =============================================

    private RecentView createRecentView(Long id, Long hospitalId, String name,
                                        String address, LocalDateTime viewedAt) {
        RecentView rv = new RecentView();
        ReflectionTestUtils.setField(rv, "id", id);
        ReflectionTestUtils.setField(rv, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(rv, "name", name);
        ReflectionTestUtils.setField(rv, "address", address);
        ReflectionTestUtils.setField(rv, "viewedAt", viewedAt);
        return rv;
    }
}