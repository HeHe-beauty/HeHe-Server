package org.dev.hehe.domain.hospital;

import lombok.Getter;

/**
 * 병원 태그 도메인 (tb_hospital_tag 매핑)
 * MyBatis가 리플렉션으로 주입. 태그 일괄 조회 후 서비스에서 hospitalId 기준으로 그룹핑
 */
@Getter
public class HospitalTag {

    /** 태그가 속한 병원 ID */
    private Long hospitalId;

    /** 태그 내용 (예: 남성원장, 주차가능) */
    private String tagName;
}