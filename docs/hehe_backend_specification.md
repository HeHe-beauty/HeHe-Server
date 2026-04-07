## 1. 기능별 카테고리화 및 요구사항 상세화

요구사항을 도메인 단위로 묶고, 백엔드 로직을 상세화

| **카테고리**                 | **주요 요구사항 (상세)**                                    | **백엔드 핵심 로직**                              |
| ------------------------ | --------------------------------------------------- | ------------------------------------------ |
| **회원 및 인증 (User)**       | 소셜 로그인(카카오/네이버), 프로필 조회, 알림 설정(푸시/야간/마케팅), 회원 탈퇴    | JWT 기반 인증, OAuth2 연동, 유저별 설정 값(Boolean) 관리 |
| **병원 및 지도 (Hospital)**   | 위치 기반 병원 조회, 제모 기기 대수 노출, 클러스터링 데이터(확대/축소), 역/주소 검색 | 공간 쿼리(Spatial Query) 최적화, 역 좌표 DB 구축       |
| **사용자 활동 (Interaction)** | 병원 찜하기(Bookmark), 최근 본 병원, 문의 내역, 찜 상태 토글           | Redis(최근 본 목록) 활용, 찜 중복 체크 및 삭제 로직         |
| **캘린더 및 예약 (Schedule)**  | 일정 추가/수정, 예약일 표시, 알림 설정(Check), 7일 요약 정보            | 날짜 범위 쿼리, 알림 스케줄러(Batch/Task) 연동           |
| **콘텐츠 및 공통 (Content)**   | 추천 아티클(횡 스크롤), 메인 노출 기기 관리, 공지사항                    | 관리자 지정 우선순위 정렬, 아티클 메타데이터 제공               |



- 개발 스펙
  
  - Java 17
  
  - Spring boot 3
  
  - MySQL 8
  
  - MyBatis
  
  - Redis





---

## 2. API 설계

화면 요구사항에서 추출된 서버의 역할들을 REST API로 구성

### **[Common]**

- 서버 현재시각 응답 api : 서버의 시각을 리턴하면됨

### **[User / Auth]**

- `POST /api/v1/auth/login/{provider}` : 소셜 로그인 및 회원가입
- `GET /api/v1/users/me` : 내 정보(닉네임, 아이콘) 및 설정 현황 조회
- `PATCH /api/v1/users/me/settings` : 알림 동의 여부 업데이트
- `POST /api/v1/auth/logout` : 로그아웃 (토큰 무효화)

### **[Hospital / Map]**

- `GET /api/v1/hospitals/map` : 현재 지도 영역 내 병원 핀 정보(좌표, 기기수, 태그) 조회
- `GET /api/v1/hospitals/search?keyword={name}` : 역 이름 또는 주소 기반 검색
- `GET /api/v1/hospitals/{id}` : 특정 병원 상세 정보 및 문의처 조회

### **[Schedule / Calendar]**

- `GET /api/v1/schedules/summary?start={date}` : 홈 화면용 7일간의 예약 존재 여부 조회
- `GET /api/v1/schedules?month={YYYY-MM}` : 월별 전체 예약일 조회
- `POST /api/v1/schedules` : 방문 일정 추가 (병원명, 일시, 알림 설정)
- `PUT /api/v1/schedules/{id}` : 일정 수정 및 알림 설정 변경

### **[Interactions & Content]**

- `POST /api/v1/bookmarks/{hospitalId}` : 병원 찜하기/해제 (Toggle)
- `GET /api/v1/bookmarks` : 찜한 병원 목록 조회
- `GET /api/v1/articles` : 메인 추천 콘텐츠 목록 조회

> **[미구현 - 추후 추가 필요]**
> - `DELETE /api/v1/users/me` : 회원 탈퇴 (카카오/네이버 Unlink 연동 포함)
> - 문의 내역 API : 테이블 설계 및 API 미정
> - 역/주소 검색용 데이터 출처 미결정 (공공 API 연동 or 직접 DB 구축)

---

## 3. 데이터 모델링 (ERD 초안)

HEHE 서비스를 위한 핵심 엔티티 구조

- 디테일한 인덱스는 추후에 추가
- FK 는 미적용
- tb_push_history 는 추후에 디벨롭하여 적용
- 모든 테이블에 created_at(timestamp), updated_at(timestamp) 있으나 설명에서는 생략

## 3.1. 엔티티별 상세 명세

### **3.1.1. 회원 및 인증 도메인 (User & Auth)**

### **tb_user (유저)**

| **컬럼명**        | **타입**       | **제약사항**   | **설명**                              |
| -------------- | ------------ | ---------- | ----------------------------------- |
| `id`           | BigInt       | PK, Auto   | id                                  |
| `user_id`      | BigInt       | Unique     | user id                             |
| `social_id`    | Varchar(255) | Not Null   | 카카오/네이버에서 제공하는 고유 ID                |
| `provider`     | Enum         | Not Null   | 소셜 플랫폼 구분 (KAKAO, NAVER)            |
| `email`        | Varchar(100) | Unique     | 소셜 계정 이메일                           |
| `nickname`     | Varchar(50)  | Not Null   | 서비스 활동 닉네임                          |
| `fcm_token`    | Varchar(255) | -          | 푸시 알림 발송용 토큰                        |
| `push_agreed`  | Boolean      | Default(F) | 일반 푸시 수신 동의 여부                      |
| `night_agreed` | Boolean      | Default(F) | 야간 푸시 수신 동의 여부                      |
| `mkt_agreed`   | Boolean      | Default(F) | 마케팅 정보 수신 동의 여부                     |
| `status`       | Enum         | Not Null   | ACTIVE(활성), LEAVE(탈퇴유예), BANNED(정지) |

- **Unique**
  - `(social_id, provider)` : 소셜 가입 중복방지
  - `user_id`
- **Index**
  - `fcm_token` : 로그아웃이나 토큰 만료 시 빈번하게 조회/업데이트 가능성

### **3.1.2. 병원 및 기기 도메인**

병원 및 기기 관련 도메인 테이블

### **<병원 관련 도메인>**

### **tb_hospital (병원)**

| **컬럼명**          | **타입**            | **제약사항** | **설명**                          |
|-----------------|-------------------|----------|----------------------------------|
| `id`            | BigInt            | PK, Auto | id                               |
| `hospital_id`   | BigInt            | Unique   | 병원 ID                            |
| `name`          | Varchar(100)      | Not Null | 병원 명칭                            |
| `address`       | Varchar(255)      | Not Null | 도로명 주소                           |
| `location`      | Point (SRID 4326) | Not Null | 위도/경도 좌표 데이터 (WGS84 Spatial 데이터) |
| `contact_url`   | Varchar(255)      | -        | 외부 문의/예약 링크                      |
| `contact_number` | Varchar(255)     | Not Null | 문의 전화 번호                         |

- **Unique**
  - `hospital_id`
- **Index**
  - `location` : SPATIAL INDEX. 뷰포트 바운딩박스 쿼리(`MBRContains`) 및 좌표 기반 클러스터링 필수
  - `name` : FULLTEXT INDEX. 병원명 키워드 검색용

### **tb_hospital_tag (병원별 특징 태그)**

| **컬럼명**       | **타입**      | **제약사항** | **설명**                |
| ------------- | ----------- | -------- | --------------------- |
| `id`          | BigInt      | PK, Auto | id                    |
| `hospital_id` | BigInt      | Not Null | `tb_hospital` 참조      |
| `tag_name`    | Varchar(50) | Not Null | 태그 내용 (예: 남성원장, 주차가능) |

- **Unique**
  - `(hospital_id, tag_name)` : 동일 병원에 같은 태그 중복 방지
- **Index**
  - `tag_name` : 추후에 특정 태그(예: '야간진료') 필터링 성능을 위한 적용

### <시술 및 장비 도메인>

### **tb_category (대분류)**

보톡스, 제모, 피부관리 등의 대분류 카테고리

| **컬럼명**         | **타입**      | **제약사항**   | **설명**            |
| --------------- | ----------- | ---------- | ----------------- |
| `category_id`   | BigInt      | PK, Auto   | 카테고리 식별자          |
| `name`          | Varchar(50) | Not Null   | 카테고리명 (제모, 리프팅 등) |
| `is_display`    | tinyint     |            | 노출 여부             |
| `display_order` | Integer     | Default(0) | 화면 노출 순번          |

### **tb_equipment (장비)**

| **컬럼명**           | **타입**       | **제약사항**   | **설명**              |
|-------------------| ------------ | ---------- | ------------------- |
| `equip_id`        | BigInt       | PK, Auto   | 장비 식별자              |
| `model_name`      | Varchar(100) | Not Null   | 장비 모델명 (아포지 플러스 등)  |
| `is_main_display` | Boolean      | Default(F) | 홈 화면 노출 여부 (어드민 설정) |
| `display_order`   | Integer      | -          | 홈 화면 노출 순서          |

### **tb_procedure (시술)**

| **컬럼명**        | **타입**       | **제약사항** | **설명**                          |
| -------------- | ------------ | -------- | ------------------------------- |
| `id`           | Bigint       | PK, Auto |                                 |
| `procedure_id` | BigInt       | Unique   | 시술 식별자                          |
| `category_id`  | BigInt       | -        | `tb_category` 참조. 추후에 적용        |
| `equip_id`     | BigInt       | -        | `tb_equipment` 참조. 기기 없는 시술은 NULL |
| `name`         | Varchar(100) | Not Null | 시술 표준 명칭                        |
| `description`  | Text         | -        | 시술 상세 설명 및 효과                   |

- **Unique**
  - `procedure_id`
- **Index**
  - `category_id`
  - `equip_id`

### <매핑 도메인>

### **tb_hospital_procedure (병원별 시술/가격)**

| **컬럼명**        | **타입**  | **제약사항**     | **설명**            |
| -------------- | ------- | ------------ | ----------------- |
| `hospital_id`  | BigInt  | PK, Not Null | `tb_hospital` 참조  |
| `procedure_id` | BigInt  | PK, Not Null | `tb_procedure` 참조 |
| `price`        | Integer | Not Null     | 해당 병원의 시술 가격      |
| `duration`     | Integer | -            | 시술 소요 시간          |

- **PK**
  - `(hospital_id, procedure_id)`

### **tb_hospital_equipment (병원별 기기 보유 현황)**

| **컬럼명**       | **타입**  | **제약사항**     | **설명**            |
| ------------- | ------- | ------------ | ----------------- |
| `hospital_id` | BigInt  | PK, Not Null | `tb_hospital` 참조  |
| `equip_id`    | BigInt  | PK, Not Null | `tb_equipment` 참조 |
| `total_count` | Integer | Default(0)   | 해당 병원이 보유한 기기 대수  |

- **PK**
  - `(hospital_id, equip_id)`

### **3.1.3. 일정 및 활동 도메인 (Schedule & Interaction)**

유저의 캘린더 데이터와 개인화된 리스트

### **tb_schedule (캘린더 방문 일정)**

| **컬럼명**           | **타입**       | **제약사항**   | **설명**                                        |
| ----------------- | ------------ | ---------- | --------------------------------------------- |
| `id`              | BigInt       | PK, Auto   | id                                            |
| `schedule_id`     | BigInt       | Unique     | 일정 id                                         |
| `user_id`         | BigInt       | Not Null   | 사용자 id                                        |
| `hospital_name`   | Varchar(100) | Not Null   | 일정 대상 병원명 (직접 입력, 추후 hospital_id FK 연결 예정)    |
| `procedure_name`  | Varchar(100) | Null       | 시술명 (직접 입력, 추후 procedure_id FK 연결 예정)         |
| `visit_time`      | `BIGINT`     | NOT NULL   | 방문 예정 시간 (Unix timestamp, 시간대 전환은 FE에서 처리)    |
| `alarm_enabled`   | Boolean      | Default(T) | 해당 일정 푸시 알림 여부                                |

> 장기 마이그레이션 계획: hospital_id(BIGINT NULL), procedure_id(BIGINT NULL) FK 컬럼 추가 예정.
> FK 연결 시 id + name 모두 저장 (name은 스냅샷 역할 — 원본 데이터 변경 후에도 일정 기록 유지)

- **Unique**
  - `schedule_id`
- **Index**
  - `(user_id, visit_time)` **:** 유저 캘린더 조회시 필요

### **tb_schedule_alarm (캘린더 방문 일정)**

tb_schedule 테이블과 1:N 관계

| **컬럼명**       | **타입**   | **제약사항**   | **설명**       |
| ------------- | -------- | ---------- |--------------|
| `id`          | BigInt   | PK, Auto   | id           |
| `schedule_id` | BigInt   | Not Null       | 일정 id        |
| `alarm_type`  | ENUM('1H', '3H', '1D', 'CUSTOM')   | Not Null   | 알림 유형        |
| `alarm_time`  | `BIGINT` | NOT NULL   | 알림 발송 예정 시간  |
| `is_sent`     | Boolean  | Default(T) | 발송 완료 여부 |

- **Index**
  - `schedule_id`
  - `(alarm_time, is_sent)` **:** 유저 캘린더 조회시 필요

### **tb_bookmark (병원 찜하기)**

| **컬럼명**       | **타입** | **제약사항**     | **설명** |
| ------------- | ------ | ------------ | ------ |
| `user_id`     | BigInt | PK, Not Null | 유저 ID  |
| `hospital_id` | BigInt | PK, Not Null | 병원 ID  |

- **PK**
  - `(user_id, hospital_id)`

### **3.2.4. 운영 및 콘텐츠 도메인**

### **tb_article (추천 아티클)**

| **컬럼명**         | **타입**       | **제약사항**   | **설명**                         |
| --------------- | ------------ | ---------- | ------------------------------ |
| `id`            | BigInt       | PK, Auto   | id                             |
| `article_id`    | BigInt       | Unique     | 아티클 ID                         |
| `title`         | Varchar(200) | Not Null   | 아티클 제목                         |
| `sub_title`     | Varchar(255) | -          | 리스트에 노출될 짧은 요약 문구              |
| `thumbnail_url` | Varchar(255) | Not Null   | 홈 화면 썸네일 이미지 경로                |
| `content`       | MEDIUMTEXT   | Not Null   | 아티클 본문 HTML (어드민 리치 에디터로 입력, 앱에서 WebView 렌더링) |
| `priority`      | Integer      | Default(0) | 홈 화면 노출 우선순위 (높을수록 앞 순서)       |
| `is_active`     | Boolean      | Default(T) | 노출 여부 (어드민에서 제어)               |
| `view_count`    | Integer      | Default(0) | 조회수 (인기 콘텐츠 분석용)               |

- **Unique**
  - `article_id`
- **Index**
  - `(is_active, priority DESC)` **:** 추후 우선 순위 높은 아티클 순으로 정렬기능이 추가 될때 적용

### **tb_push_history (알림 발송 이력)**

FCM을 통한 알림 발송 내역을 관리하는 어드민 전용 테이블입니다.

| **컬럼명**         | **타입**       | **제약사항** | **설명**                                   |
| --------------- | ------------ | -------- | ---------------------------------------- |
| `push_id`       | BigInt       | PK, Auto | 발송 이력 식별자                                |
| `title`         | Varchar(100) | Not Null | 푸시 제목                                    |
| `body`          | Text         | Not Null | 푸시 본문 내용                                 |
| `target_type`   | Enum         | Not Null | ALL(전체), INDIVIDUAL(개별), TOPIC(특정기기유저 등) |
| `target_list`   | Varchar(100) | -        | 개별인 경우 저장                                |
| `sent_at`       | DateTime     | Not Null | 발송 시각                                    |
| `success_count` | Integer      | -        | 발송 성공 수                                  |

- **Index**
  - `sent_at` **:** 어드민에서 최신 발송 순으로 내역을 조회할 때 사용

---

## 4. 어드민(Admin) 관리 기능 리스트업

서비스에 필요한 백엔드 데이터 조작을 위한 어드민 개발

| 카테고리      | 주요 관리 기능              | 비고  |
| --------- | --------------------- | --- |
| **알림 관리** | - 전체/개별 푸시 발송 및 예약 설정 |     |

- 알림 템플릿(문구) 관리
- 발송 이력 및 클릭률 확인 | 발송 대상 필터링 기능 포함 |
  | **유저 관리** | - 유저 활동 상태 조회(활성/탈퇴 예정/정지)
- 유저별 문의 내역 및 조치
- 특정 유저 강제 로그아웃/제재 | 개인정보 마스킹 처리 주의 |
  | **추천 콘텐츠** | - 아티클 등록/수정/삭제 및 노출 순서(Priority) 변경
- 아티클별 클릭수 통계 확인 | 횡 스크롤 순서 커스텀 |
  | **메인 기기 관리** | - DB의 모든 기기 중 메인 노출 여부(Boolean) 토글
- 노출 기기 종류 및 순서 관리
- 메인 기기별 아이콘/대표 이미지 관리 | 홈 화면 API 연동 |

---

## 5. FCM 푸시 알림 도입 및 활용 계획서

### **1단계: FCM 작동 순서**

1. **토큰 획득:** 사용자가 앱을 설치하면 클라이언트(App)가 FCM 서버로부터 전용 **'FCM 토큰'**을 발급 받음
2. **토큰 저장:** 앱은 이 토큰을 서버 API로 전달하고, 서버는 이를 **`User` 테이블**에 저장
3. **메시지 전송:** 서버가 FCM Admin SDK를 이용해 "이 토큰(유저)에게 이런 메시지를 보내줘"라고 FCM 서버에 요청
4. **전달:** FCM 서버가 실제 사용자 기기로 푸시 전달

### **2단계: 서버 구현 로직**

- **토큰 관리 API:** 로그인 시점에 FCM 토큰을 서버에 동기화하는 API 필요(토큰은 주기적으로 갱신될 수 있으므로 업데이트 로직 포함)
- **알림 동의 체크:** 요구사항에 있던 '푸시/야간/마케팅' 동의 여부를 DB에서 확인한 뒤, 동의한 유저에게만 발송 로직 실행
- **비동기 처리 :** 외부 API 호출이므로, 사용자 응답 속도에 영향을 주지 않도록 **비동기(`@Async`)** 로 전송

### **3단계: 알림 시나리오 설계**

서비스에서 필요한 알림 시퀀스

- **예약 리마인드:** 예약 시간 n시간 전 자동 발송 (스케줄러 작업)
- **문의 답변 알림:** 병원 문의에 대한 답변이 달렸을 때 실시간 발송 (다음 버젼에서 구현)
- **마케팅 푸시:** 어드민에서 특정 타겟에게 새로운 아티클이나 이벤트 전송

---

## 6. 소셜 로그인 연동 및 인증 설계

HEHE 서비스의 사용자 편의성을 극대화하기 위한 OAuth 2.0 기반 인증 프로세스

### **6.1. 인증 아키텍처 (OAuth 2.0 & JWT)**

백엔드 서버는 카카오/네이버로부터 받은 정보를 검증하고, 우리 서비스 전용 **자체 토큰(JWT)**을 발급하는 역할

1. **인가 코드 수신:** 앱(클라이언트)에서 받은 `Authorization Code`를 서버 API가 전달받음.
2. **Access Token 교환:** 서버가 해당 코드를 카카오/네이버 서버에 보내서 `Access Token`을 획득.
3. **유저 정보 획득:** 획득한 토큰으로 유저의 `Social ID(고유값)`, `Email`, `Nickname` 등을 조회.
4. **회원가입/로그인 처리:** - DB에 해당 `Social ID`가 없으면 자동 회원가입.
   - DB에 있으면 기존 유저 정보를 가져옴.
5. **자체 JWT 발급:** 서비스 전용 `Access Token`과 `Refresh Token`을 생성하여 클라이언트에 응답.

### **6.2. 상세 개발 계획**

**1) 플랫폼별 Provider 구현 (Strategy Pattern)**

- 카카오와 네이버는 API 주소와 응답 JSON 형식이 다릅니다.
- `OAuthService` 인터페이스를 만들고, `KakaoOAuthService`, `NaverOAuthService`로 나누어 구현하면 향후 애플 로그인 등을 추가할 때 코드 수정 없이 확장 가능합니다.

**2) 보안 및 토큰 관리**

- **Refresh Token 저장:** 유저 세션 유지를 위해 DB 혹은 Redis에 `Refresh Token`을 저장하고, 만료 시 재발급 로직을 구현합니다.
- **State 검증:** 네이버 로그인의 경우 위변조 방지를 위해 `state` 값을 검증하는 로직이 필수입니다.

**3) 회원탈퇴 시 연동 해제**

- 요구사항의 '회원탈퇴' 기능을 구현할 때, 우리 DB에서 데이터를 삭제/휴면 처리함과 동시에 **카카오/네이버 연동 해제(Unlink) API**를 호출하여 보안 연결을 끊어주는 처리가 필요합니다.
