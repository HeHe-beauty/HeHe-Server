package org.dev.hehe.service.contact;

import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.domain.contact.ContactHistory;
import org.dev.hehe.domain.hospital.HospitalTag;
import org.dev.hehe.dto.contact.ContactHistoryResponse;
import org.dev.hehe.dto.contact.ContactSaveRequest;
import org.dev.hehe.mapper.bookmark.BookmarkMapper;
import org.dev.hehe.mapper.contact.ContactMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ContactService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService 테스트")
class ContactServiceTest {

    @Mock
    private ContactMapper contactMapper;

    @Mock
    private HospitalMapper hospitalMapper;

    @Mock
    private BookmarkMapper bookmarkMapper;

    @InjectMocks
    private ContactService contactService;

    @Test
    @DisplayName("문의 내역 조회 성공")
    void getContactHistories_success() {
        // given
        ContactHistory c1 = createContactHistory(1L, 101L, "강남 제모 클리닉", "CALL",
                LocalDateTime.of(2026, 4, 22, 10, 30));
        ContactHistory c2 = createContactHistory(2L, 102L, "역삼 스킨케어", "VISIT",
                LocalDateTime.of(2026, 4, 20, 15, 0));

        HospitalTag tag = new HospitalTag();
        ReflectionTestUtils.setField(tag, "hospitalId", 101L);
        ReflectionTestUtils.setField(tag, "tagName", "여성원장");

        given(contactMapper.findContactHistories(1L)).willReturn(List.of(c1, c2));
        given(hospitalMapper.findTagsByHospitalIds(List.of(101L, 102L))).willReturn(List.of(tag));
        given(bookmarkMapper.findBookmarkedHospitalIds(1L, List.of(101L, 102L))).willReturn(List.of(101L));

        // when
        List<ContactHistoryResponse> result = contactService.getContactHistories(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getHospitalName()).isEqualTo("강남 제모 클리닉");
        assertThat(result.get(0).getContactType()).isEqualTo("CALL");
        assertThat(result.get(0).getTags()).containsExactly("여성원장");
        assertThat(result.get(0).getIsBookmarked()).isTrue();
        assertThat(result.get(1).getContactType()).isEqualTo("VISIT");
        assertThat(result.get(1).getIsBookmarked()).isFalse();
    }

    @Test
    @DisplayName("문의 내역 없음 - 빈 리스트 반환")
    void getContactHistories_empty() {
        // given
        given(contactMapper.findContactHistories(1L)).willReturn(List.of());

        // when
        List<ContactHistoryResponse> result = contactService.getContactHistories(1L);

        // then
        assertThat(result).isEmpty();
        verify(contactMapper).findContactHistories(1L);
    }

    @Test
    @DisplayName("문의 저장 성공")
    void saveContact_success() {
        // given
        ContactSaveRequest request = new ContactSaveRequest();
        ReflectionTestUtils.setField(request, "hospitalId", 101L);
        ReflectionTestUtils.setField(request, "contactType", "CALL");

        // when
        contactService.saveContact(1L, request);

        // then
        verify(contactMapper, times(1)).insertContact(1L, 101L, "CALL");
    }

    @Test
    @DisplayName("문의 삭제 성공")
    void deleteContact_success() {
        // given
        given(contactMapper.existsContact(1L, 1L)).willReturn(true);

        // when
        contactService.deleteContact(1L, 1L);

        // then
        verify(contactMapper, times(1)).softDeleteContact(1L, 1L);
    }

    @Test
    @DisplayName("문의 삭제 실패 - 존재하지 않는 문의 내역 (CO001)")
    void deleteContact_notFound() {
        // given
        given(contactMapper.existsContact(999L, 1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> contactService.deleteContact(999L, 1L))
                .isInstanceOf(CommonException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTACT_NOT_FOUND);

        verify(contactMapper, never()).softDeleteContact(999L, 1L);
    }

    // =============================================
    // 헬퍼 메서드
    // =============================================

    private ContactHistory createContactHistory(Long id, Long hospitalId, String hospitalName,
                                                String contactType, LocalDateTime createdAt) {
        ContactHistory contact = new ContactHistory();
        ReflectionTestUtils.setField(contact, "id", id);
        ReflectionTestUtils.setField(contact, "hospitalId", hospitalId);
        ReflectionTestUtils.setField(contact, "hospitalName", hospitalName);
        ReflectionTestUtils.setField(contact, "contactType", contactType);
        ReflectionTestUtils.setField(contact, "createdAt", createdAt);
        return contact;
    }
}