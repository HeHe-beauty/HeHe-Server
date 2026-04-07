package org.dev.hehe.dto.common;

import lombok.Builder;
import lombok.Getter;

/**
 * 서버 현재 시각 응답 DTO
 *
 * - timestamp : Unix timestamp (밀리초) - 클라이언트 시간 동기화용
 * - datetime  : 사람이 읽기 쉬운 포맷 (UTC+9 기준)
 */
@Getter
@Builder
public class ServerTimeResponse {

    private long timestamp;
    private String datetime;
}