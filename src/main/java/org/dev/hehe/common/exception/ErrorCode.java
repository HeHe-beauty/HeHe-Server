package org.dev.hehe.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전체에서 사용하는 에러 코드 정의
 *
 * 코드 prefix 규칙:
 *  - C  : Common (공통)
 *  - A  : Article (아티클)
 *  - U  : User (유저)
 *  - H  : Hospital / Equipment / Procedure (병원·기기·시술 도메인 통합)
 *  - S  : Schedule (일정)
 *  - AU : Auth (인증)
 */
@Getter
public enum ErrorCode {

    /* ========================
     * C : Common
     * ======================== */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청 값입니다."),

    /* ========================
     * A : Article
     * ======================== */
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "아티클을 찾을 수 없습니다."),

    /* ========================
     * U : User
     * ======================== */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다."),

    /* ========================
     * H : Hospital
     * ======================== */
    HOSPITAL_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "병원을 찾을 수 없습니다."),
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "H002", "기기를 찾을 수 없습니다."),

    /* ========================
     * S : Schedule
     * ======================== */
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "일정을 찾을 수 없습니다."),
    ALARM_ALREADY_EXISTS(HttpStatus.CONFLICT, "S002", "이미 등록된 알림 유형입니다."),
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "S003", "등록된 알림을 찾을 수 없습니다."),

    /* ========================
     * AU : Auth
     * ======================== */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AU001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AU002", "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU003", "인증 정보가 없습니다."),
    OAUTH_USER_INFO_FAILED(HttpStatus.BAD_GATEWAY, "AU004", "소셜 유저 정보 조회에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}