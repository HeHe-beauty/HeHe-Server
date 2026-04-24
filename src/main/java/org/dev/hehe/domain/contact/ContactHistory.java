package org.dev.hehe.domain.contact;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 문의 내역 도메인 (tb_contact_history JOIN tb_hospital 결과)
 */
@Getter
@NoArgsConstructor
public class ContactHistory {

    /** 문의 내역 ID */
    private Long id;

    /** 병원 ID */
    private Long hospitalId;

    /** 병원명 (tb_hospital JOIN) */
    private String hospitalName;

    /** 병원 주소 (tb_hospital JOIN) */
    private String address;

    /** 문의 유형 (CALL/CHAT/VISIT) */
    private String contactType;

    /** 문의 시각 */
    private LocalDateTime createdAt;
}