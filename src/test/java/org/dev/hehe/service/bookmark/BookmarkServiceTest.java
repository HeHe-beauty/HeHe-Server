package org.dev.hehe.service.bookmark;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.bookmark.BookmarkedHospital;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.dto.bookmark.BookmarkResponse;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.hospital.HospitalMapper;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * BookmarkService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService 테스트")
class BookmarkServiceTest {

    @Mock
    private BookmarkMapper bookmarkMapper;

    @Mock
    private HospitalMapper hospitalMapper;

    @InjectMocks
    private BookmarkService bookmarkService;

    // =============================================
    // getBookmarks 테스트
    // =============================================

    @Test
    @DisplayName("찜 목록 조회 성공 - 태그 포함")
    void getBookmarks_success_withTags() {
        // given
        LocalDateTime bookmarkedAt1 = LocalDateTime.of(2026, 4, 22, 10, 30);
        LocalDateTime bookmarkedAt2 = LocalDateTime.of(2026, 4, 20, 15, 0);

        BookmarkedHospital h1 = createBookmarkedHospital(101L, "강남 제모 클리닉", "서울 강남구 역삼동 1", bookmarkedAt1);
        BookmarkedHospital h2 = createBookmarkedHospital(102L, "역삼 스킨케어", "서울 강남구 역삼동 2", bookmarkedAt2);

        HospitalTag tag1 = createTag(101L, "여성원장");
        HospitalTag tag2 = createTag(101L, "주차가능");
        HospitalTag tag3 = createTag(102L, "야간진료");

        given(bookmarkMapper.findBookmarkedHospitals(1L)).willReturn(List.of(h1, h2));
        given(hospitalMapper.findTagsByHospitalIds(List.of(101L, 102L))).willReturn(List.of(tag1, tag2, tag3));

        // when
        List<BookmarkResponse> result = bookmarkService.getBookmarks(1L);

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getHospitalId()).isEqualTo(101L);
        assertThat(result.get(0).getName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.get(0).getTags()).containsExactlyInAnyOrder("여성원장", "주차가능");
        assertThat(result.get(0).getBookmarkedAt()).isEqualTo(bookmarkedAt1);

        assertThat(result.get(1).getHospitalId()).isEqualTo(102L);
        assertThat(result.get(1).getTags()).containsExactly("야간진료");
        assertThat(result.get(1).getBookmarkedAt()).isEqualTo(bookmarkedAt2);
    }

    @Test
    @DisplayName("찜 목록 조회 성공 - 태그 없는 병원 포함")
    void getBookmarks_success_withNoTags() {
        // given
        BookmarkedHospital h1 = createBookmarkedHospital(101L, "강남 제모 클리닉", "서울 강남구 역삼동 1",
                LocalDateTime.of(2026, 4, 22, 10, 30));

        given(bookmarkMapper.findBookmarkedHospitals(1L)).willReturn(List.of(h1));
        given(hospitalMapper.findTagsByHospitalIds(List.of(101L))).willReturn(List.of());

        // when
        List<BookmarkResponse> result = bookmarkService.getBookmarks(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTags()).isEmpty();
    }

    @Test
    @DisplayName("찜 목록 조회 성공 - 찜한 병원 없음 (빈 리스트)")
    void getBookmarks_empty() {
        // given
        given(bookmarkMapper.findBookmarkedHospitals(1L)).willReturn(List.of());

        // when
        List<BookmarkResponse> result = bookmarkService.getBookmarks(1L);

        // then
        assertThat(result).isEmpty();
        verify(bookmarkMapper).findBookmarkedHospitals(1L);
        // 병원 없으면 태그 조회 쿼리 실행 안 함
        verifyNoMoreInteractions(hospitalMapper);
    }

    // =============================================
    // addBookmark 테스트
    // =============================================

    @Test
    @DisplayName("찜 추가 성공")
    void addBookmark_success() {
        // given
        given(bookmarkMapper.existsBookmark(1L, 101L)).willReturn(false);

        // when
        bookmarkService.addBookmark(1L, 101L);

        // then
        verify(bookmarkMapper).insertBookmark(1L, 101L);
    }

    @Test
    @DisplayName("찜 추가 실패 - 이미 찜한 병원 → B001 (409)")
    void addBookmark_alreadyExists() {
        // given
        given(bookmarkMapper.existsBookmark(1L, 101L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> bookmarkService.addBookmark(1L, 101L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.BOOKMARK_ALREADY_EXISTS));

        then(bookmarkMapper).should(never()).insertBookmark(1L, 101L);
    }

    // =============================================
    // removeBookmark 테스트
    // =============================================

    @Test
    @DisplayName("찜 삭제 성공")
    void removeBookmark_success() {
        // given
        given(bookmarkMapper.existsBookmark(1L, 101L)).willReturn(true);

        // when
        bookmarkService.removeBookmark(1L, 101L);

        // then
        verify(bookmarkMapper).deleteBookmark(1L, 101L);
    }

    @Test
    @DisplayName("찜 삭제 실패 - 찜하지 않은 병원 → B002 (404)")
    void removeBookmark_notFound() {
        // given
        given(bookmarkMapper.existsBookmark(1L, 101L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookmarkService.removeBookmark(1L, 101L))
                .isInstanceOf(CommonException.class)
                .satisfies(e -> assertThat(((CommonException) e).getErrorCode())
                        .isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND));

        then(bookmarkMapper).should(never()).deleteBookmark(1L, 101L);
    }

    // =============================================
    // 헬퍼 메서드
    // =============================================

    private BookmarkedHospital createBookmarkedHospital(Long hospitalId, String name, String address,
                                                        LocalDateTime bookmarkedAt) {
        BookmarkedHospital hospital = new BookmarkedHospital();
        ReflectionTestUtils.setField(hospital, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(hospital, "name", name);
        ReflectionTestUtils.setField(hospital, "address", address);
        ReflectionTestUtils.setField(hospital, "bookmarkedAt", bookmarkedAt);
        return hospital;
    }

    private HospitalTag createTag(Long hospitalId, String tagName) {
        HospitalTag tag = new HospitalTag();
        ReflectionTestUtils.setField(tag, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(tag, "tagName", tagName);
        return tag;
    }
}