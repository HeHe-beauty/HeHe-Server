package org.dev.hehe.controller.contact;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dev.hehe.common.response.ApiResult;
import org.dev.hehe.config.auth.LoginUser;
import org.dev.hehe.dto.contact.ContactHistoryResponse;
import org.dev.hehe.dto.contact.ContactSaveRequest;
import org.dev.hehe.service.contact.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 문의 내역 컨트롤러
 * Swagger 명세는 ContactApiSpecification 인터페이스 참고
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController implements ContactApiSpecification {

    private final ContactService contactService;

    /** 문의 내역 목록 조회 */
    @Override
    @GetMapping
    public ApiResult<List<ContactHistoryResponse>> getContactHistories(@LoginUser Long userId) {
        log.info("[GET] /api/v1/contacts - userId={}", userId);
        List<ContactHistoryResponse> response = contactService.getContactHistories(userId);
        log.info("문의 내역 조회 완료 - userId={}, count={}", userId, response.size());
        return ApiResult.ok(response);
    }

    /** 문의 내역 저장 */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Void> saveContact(@LoginUser Long userId, @Valid @RequestBody ContactSaveRequest request) {
        log.info("[POST] /api/v1/contacts - userId={}, hospitalId={}", userId, request.getHospitalId());
        contactService.saveContact(userId, request);
        return ApiResult.ok(null);
    }

    /** 문의 내역 소프트 삭제 */
    @Override
    @DeleteMapping("/{contactId}")
    public ApiResult<Void> deleteContact(@PathVariable Long contactId, @LoginUser Long userId) {
        log.info("[DELETE] /api/v1/contacts/{} - userId={}", contactId, userId);
        contactService.deleteContact(contactId, userId);
        return ApiResult.ok(null);
    }
}