
# 데이터 스키마 DDL

```sql
## hehe initial DDL

-- 2.1 회원 및 인증 도메인
CREATE TABLE if not exists tb_user
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '내부 관리용 식별자',
    user_id      BIGINT UNIQUE                       NOT NULL COMMENT '비즈니스용 유저 ID',
    social_id    VARCHAR(255)                        NOT NULL COMMENT '소셜 제공 고유 ID',
    provider     ENUM ('KAKAO', 'NAVER')             NOT NULL COMMENT '플랫폼 구분',
    email        VARCHAR(100) UNIQUE COMMENT '소셜 계정 이메일',
    nickname     VARCHAR(50)                         NOT NULL COMMENT '활동 닉네임',
    fcm_token    VARCHAR(255) COMMENT '푸시 알림 토큰',
    push_agreed  BOOLEAN   DEFAULT FALSE COMMENT '일반 푸시 동의',
    night_agreed BOOLEAN   DEFAULT FALSE COMMENT '야간 푸시 동의',
    mkt_agreed   BOOLEAN   DEFAULT FALSE COMMENT '마케팅 동의',
    status       ENUM ('ACTIVE', 'LEAVE', 'BANNED')  NOT NULL COMMENT '계정 상태',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_social_id_provider (social_id, provider),
    INDEX idx_fcm_token (fcm_token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.2 병원 및 기기 도메인

-- 행정구역 중심 좌표 참조 테이블
-- 지도 클러스터링 시 핀 위치 산출에 사용 (병원 행마다 중복 저장 방지)
-- parent_name: SIDO는 빈 문자열, SIGUNGU는 시도명, DONG은 시군구명
CREATE TABLE tb_district
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    level       ENUM ('SIDO', 'SIGUNGU', 'DONG') NOT NULL COMMENT '행정구역 단계',
    name        VARCHAR(30)                       NOT NULL COMMENT '행정구역명 (예: 강남구)',
    parent_name VARCHAR(30)                       NOT NULL DEFAULT '' COMMENT '상위 행정구역명 (SIDO는 빈 문자열, SIGUNGU는 시도명, DONG은 시군구명)',
    center_lat  DECIMAL(10, 7)                    NOT NULL COMMENT '행정구역 중심 위도',
    center_lng  DECIMAL(10, 7)                    NOT NULL COMMENT '행정구역 중심 경도',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_district (level, name, parent_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tb_hospital
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id    BIGINT UNIQUE NOT NULL COMMENT '병원 ID',
    name           VARCHAR(100)  NOT NULL COMMENT '병원 명칭',
    address        VARCHAR(255)  NOT NULL COMMENT '도로명 주소',
    location       POINT         NOT NULL SRID 4326 COMMENT '위도/경도 좌표 데이터 (WGS84)',
    sido_name      VARCHAR(20)   NOT NULL COMMENT '시/도명 (예: 서울특별시) — 클러스터링 GROUP BY 및 tb_district JOIN 키',
    sigungu_name   VARCHAR(20)   NOT NULL COMMENT '시/군/구명 (예: 강남구) — 클러스터링 GROUP BY 및 tb_district JOIN 키',
    dong_name      VARCHAR(30)   NOT NULL COMMENT '읍/면/동명 (예: 역삼동) — 클러스터링 GROUP BY 및 tb_district JOIN 키',
    contact_url    VARCHAR(255)  COMMENT '외부 예약 링크',
    contact_number VARCHAR(255)  NOT NULL COMMENT '문의 전화번호',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    SPATIAL INDEX idx_location (location),
    FULLTEXT INDEX idx_hospital_name (name),
    INDEX idx_sido (sido_name),
    INDEX idx_sigungu (sigungu_name),
    INDEX idx_dong (dong_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- tag_id 제거, (hospital_id, tag_name) 복합 Unique 적용
CREATE TABLE tb_hospital_tag
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT       NOT NULL COMMENT 'tb_hospital 참조',
    tag_name    VARCHAR(50)  NOT NULL COMMENT '태그 내용 (예: 남성원장, 주차가능)',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hospital_tag (hospital_id, tag_name),
    INDEX idx_tag_name (tag_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tb_category
(
    category_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL COMMENT '카테고리명',
    is_display    TINYINT DEFAULT 1 COMMENT '노출 여부',
    display_order INT     DEFAULT 0 COMMENT '화면 노출 순번',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tb_equipment
(
    equip_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name      VARCHAR(100) NOT NULL COMMENT '장비 모델명',
    is_main_display BOOLEAN DEFAULT FALSE COMMENT '홈 노출 여부',
    display_order  INT COMMENT '홈 노출 순서',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tb_procedure
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    procedure_id BIGINT UNIQUE NOT NULL COMMENT '시술 식별자',
    category_id  BIGINT COMMENT 'tb_category 참조 (추후 적용)',
    equip_id     BIGINT COMMENT 'tb_equipment 참조 (기기 없는 시술의 경우 NULL 허용)',
    name         VARCHAR(100)  NOT NULL COMMENT '시술 표준 명칭',
    description  TEXT COMMENT '시술 설명',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_equip (equip_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.3 매핑 도메인
-- id 제거, (hospital_id, procedure_id) 복합 PK 적용
CREATE TABLE tb_hospital_procedure
(
    hospital_id  BIGINT NOT NULL COMMENT 'tb_hospital 참조',
    procedure_id BIGINT NOT NULL COMMENT 'tb_procedure 참조',
    price        INT    NOT NULL COMMENT '시술 가격',
    duration     INT COMMENT '소요 시간',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (hospital_id, procedure_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- id 제거, (hospital_id, equip_id) 복합 PK 적용
CREATE TABLE tb_hospital_equipment
(
    hospital_id BIGINT NOT NULL COMMENT 'tb_hospital 참조',
    equip_id    BIGINT NOT NULL COMMENT 'tb_equipment 참조',
    total_count INT DEFAULT 0 COMMENT '보유 기기 대수',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (hospital_id, equip_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.4 일정 및 활동 도메인
-- alarm_type 제거 (tb_schedule_alarm으로 1:N 분리)
CREATE TABLE tb_schedule
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id    BIGINT UNIQUE NOT NULL COMMENT '일정 ID',
    user_id        BIGINT        NOT NULL COMMENT '사용자 ID',
    hospital_name  VARCHAR(100)  NOT NULL COMMENT '일정 대상 병원명 (직접 입력, 추후 hospital_id FK 연결 예정)',
    procedure_name VARCHAR(100)  NULL COMMENT '시술명 (직접 입력, 추후 procedure_id FK 연결 예정)',
    visit_time     BIGINT        NOT NULL COMMENT '방문 예정 일시 (Unix timestamp)',
    alarm_enabled  BOOLEAN DEFAULT TRUE COMMENT '푸시 알림 마스터 토글',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_schedule (user_id, visit_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- tb_schedule 과 1:N 관계, 알림 시점을 다중으로 설정하기 위해 분리
CREATE TABLE tb_schedule_alarm
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT                                NOT NULL COMMENT 'tb_schedule 참조',
    alarm_type  ENUM ('1H', '3H', '1D', 'CUSTOM')    NOT NULL COMMENT '알림 유형',
    alarm_time  BIGINT                                NOT NULL COMMENT '알림 발송 예정 시각 (Unix timestamp)',
    is_sent     BOOLEAN DEFAULT FALSE COMMENT '발송 완료 여부',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_alarm_time_sent (alarm_time, is_sent)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tb_bookmark
(
    user_id     BIGINT NOT NULL COMMENT '유저 ID',
    hospital_id BIGINT NOT NULL COMMENT '병원 ID',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, hospital_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.5 콘텐츠 도메인
CREATE TABLE tb_article
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id    BIGINT UNIQUE NOT NULL COMMENT '아티클 ID',
    title         VARCHAR(200)  NOT NULL COMMENT '아티클 제목',
    sub_title     VARCHAR(255) COMMENT '리스트 노출용 요약 문구',
    thumbnail_url VARCHAR(255)  NOT NULL COMMENT '썸네일 이미지 경로',
    content       MEDIUMTEXT    NOT NULL COMMENT '아티클 본문 HTML (어드민 리치 에디터로 입력)',
    priority      INT     DEFAULT 0 COMMENT '홈 노출 우선순위 (높을수록 앞)',
    is_active     BOOLEAN DEFAULT TRUE COMMENT '노출 여부 (어드민 제어)',
    view_count    INT     DEFAULT 0 COMMENT '조회수',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_active_priority (is_active, priority DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

########## 푸시 개발할때 디벨롭 후 생성 ###################################################################
# CREATE TABLE tb_push_history
# (
#     push_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
#     push_title    VARCHAR(512) DEFAULT ''                NOT NULL,
#     push_content  TEXT                                   NOT NULL,
#     target_type   ENUM ('ALL', 'INDIVIDUAL', 'TOPIC')    NOT NULL,
#     target_list   VARCHAR(100),
#     sent_at       DATETIME                               NOT NULL,
#     success_count INT,
#     created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL,
#     updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
#     INDEX idx_sent_at (sent_at DESC)
# ) ENGINE = InnoDB
#   DEFAULT CHARSET = utf8mb4
#   COLLATE = utf8mb4_unicode_ci;

```