package org.dev.hehe.service.contact;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.exception.CommonException;
import org.dev.hehe.common.exception.ErrorCode;
import org.dev.hehe.dto.contact.ContactHistoryResponse;
import org.dev.hehe.dto.contact.ContactSaveRequest;
import org.dev.hehe.mapper.contact.ContactMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 문의 내역 비즈니스 로직 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMapper contactMapper;

    /**
     * 유저의 문의 내역 목록 조회
     *
     * <p>소프트 삭제된 항목은 제외하고, 최신 문의 순으로 반환한다.</p>
     *
     * @param userId 조회할 유저 ID
     * @return 문의 내역 목록 (최신 문의 순)
     */
    public List<ContactHistoryResponse> getContactHistories(Long userId) {
        log.debug("문의 내역 조회 - userId={}", userId);

        List<ContactHistoryResponse> result = contactMapper.findContactHistories(userId)
                .stream()
                .map(ContactHistoryResponse::from)
                .toList();

        log.debug("문의 내역 조회 완료 - userId={}, count={}", userId, result.size());
        return result;
    }

    /**
     * 문의 내역 저장
     *
     * @param userId  JWT에서 추출한 유저 ID
     * @param request 병원 ID, 문의 유형(CALL/CHAT/VISIT)
     */
    public void saveContact(Long userId, ContactSaveRequest request) {
        log.info("문의 저장 - userId={}, hospitalId={}, contactType={}", userId, request.getHospitalId(), request.getContactType());
        contactMapper.insertContact(userId, request.getHospitalId(), request.getContactType());
        log.info("문의 저장 완료 - userId={}, hospitalId={}", userId, request.getHospitalId());
    }

    /**
     * 문의 내역 소프트 삭제
     *
     * <p>해당 유저의 문의 내역이 존재하지 않거나 이미 삭제된 경우 CO001 예외를 발생시킨다.</p>
     *
     * @param contactId 삭제할 문의 내역 ID
     * @param userId    JWT에서 추출한 유저 ID
     * @throws CommonException CO001 - 문의 내역을 찾을 수 없는 경우
     */
    public void deleteContact(Long contactId, Long userId) {
        log.info("문의 삭제 요청 - contactId={}, userId={}", contactId, userId);

        if (!contactMapper.existsContact(contactId, userId)) {
            throw new CommonException(ErrorCode.CONTACT_NOT_FOUND);
        }

        contactMapper.softDeleteContact(contactId, userId);
        log.info("문의 삭제 완료 - contactId={}, userId={}", contactId, userId);
    }
}