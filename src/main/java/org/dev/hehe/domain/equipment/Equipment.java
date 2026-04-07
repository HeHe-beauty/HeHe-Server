package org.dev.hehe.domain.equipment;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * 장비 도메인 클래스
 * MyBatis 리플렉션으로 필드 주입
 */
@Getter
public class Equipment {

    private Long equipId;
    private String modelName;
    private Boolean isMainDisplay;
    private Integer displayOrder;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}