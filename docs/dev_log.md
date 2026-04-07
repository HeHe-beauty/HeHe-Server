# HEHE 백엔드 개발 기록

## 프로젝트 개요
레이저 제모 병원 찾기/예약 앱 HEHE의 백엔드 서버

- **기술 스택**: Java 17, Spring Boot 3.5, MyBatis, MySQL 8, Redis
- **패키지 루트**: `org.dev.hehe`

---

## 개발 유의사항 (필수 준수)

1. **주석 필수** — 모든 클래스, 메서드에 역할/파라미터/예외 설명 작성
2. **@Slf4j 로그 필수** — 서비스 레이어에서 조회/경고 로그 반드시 추가
3. **테스트코드 항상 함께 작성**
   - Controller: `@WebMvcTest` + `@MockitoBean`
   - Service: `@ExtendWith(MockitoExtension)` + `@InjectMocks`
   - 도메인 객체 필드 주입: `ReflectionTestUtils.setField()`
4. **Swagger 어노테이션 인터페이스 분리** — `*Api` 인터페이스에 `@Operation`, `@ApiResponse` 등 분리, Controller는 implements만
5. **@MockBean deprecated** — Spring Boot 3.4+부터 `@MockitoBean` 사용
6. **MyBatis**: XML mapper 미사용, `@Select` 어노테이션 방식, `map-underscore-to-camel-case: true`

---

## 패키지 구조

```
src/main/java/org/dev/hehe/
├── common/
│   ├── exception/       # CommonException, ErrorCode, GlobalExceptionHandler
│   └── response/        # ApiResponse<T>
├── config/              # SwaggerConfig, RestTemplateConfig
├── controller/{domain}/ # *Api (Swagger 인터페이스) + *Controller
├── service/{domain}/
├── mapper/{domain}/     # MyBatis @Mapper 인터페이스
├── dto/{domain}/        # *Response, *Request DTO
└── domain/{domain}/     # DB 매핑 도메인 객체
```

---

## DB 설계 주요 결정사항

| 테이블 | 변경 내용 |
|---|---|
| `tb_hospital_tag` | `tag_id` 제거, `UNIQUE KEY (hospital_id, tag_name)` 복합 유니크 적용 |
| `tb_hospital_procedure` | `id` 제거, `PRIMARY KEY (hospital_id, procedure_id)` 복합 PK |
| `tb_hospital_equipment` | `id` 제거, `PRIMARY KEY (hospital_id, equip_id)` 복합 PK |
| `tb_schedule` | `alarm_type` 컬럼 제거 |
| `tb_schedule_alarm` | 신규 테이블 — `alarm_type ENUM('1H','3H','1D','CUSTOM')`, `alarm_time BIGINT`, `is_sent BOOLEAN`, `schedule_id`와 1:N |
| `tb_equipment` | `is_main_exposed` → `is_main_display`, `exposure_order` → `display_order` |
| `tb_procedure` | `equip_id NOT NULL` → NULL 허용 (기기 없는 시술 고려) |

---

## 에러코드 체계

| 코드 | Enum | HTTP | 메시지 |
|---|---|---|---|
| C001 | INTERNAL_SERVER_ERROR | 500 | 서버 내부 오류가 발생했습니다. |
| C002 | INVALID_INPUT | 400 | 잘못된 요청 값입니다. |
| A001 | ARTICLE_NOT_FOUND | 404 | 아티클을 찾을 수 없습니다. |
| U001 | USER_NOT_FOUND | 404 | 유저를 찾을 수 없습니다. |
| H001 | HOSPITAL_NOT_FOUND | 404 | 병원을 찾을 수 없습니다. |
| H002 | EQUIPMENT_NOT_FOUND | 404 | 기기를 찾을 수 없습니다. |
| S001 | SCHEDULE_NOT_FOUND | 404 | 일정을 찾을 수 없습니다. |

> H prefix: Hospital / Equipment / Procedure 통합 도메인

---

## Swagger 그룹 구성

| 그룹 | 경로 패턴 |
|---|---|
| 00. 전체 | `/api/v1/**` |
| 01. Common | `/api/v1/common/**` |
| 02. User / Auth | `/api/v1/auth/**`, `/api/v1/users/**` |
| 03. Hospital | `/api/v1/hospitals/**`, `/api/v1/equipments/**` |
| 04. Schedule | `/api/v1/schedules/**` |
| 05. Interaction | `/api/v1/bookmarks/**` |
| 06. Article | `/api/v1/articles/**` |

---

## 구현 완료

### Common
- `GET /api/v1/common/time` — 서버 시각 반환

### Article
- `GET /api/v1/articles` — 활성 아티클 목록 (is_active=true, priority DESC)
- `GET /api/v1/articles/{articleId}` — 아티클 단건 조회 (없으면 A001)
- 테스트: `ArticleControllerTest`, `ArticleServiceTest`

### Equipment
- `GET /api/v1/equipments` — 메인 노출 기기 목록 (is_main_display=true, display_order ASC)
- `GET /api/v1/equipments/{equipId}` — 기기 단건 조회 (없으면 H002)
- 테스트: `EquipmentControllerTest`, `EquipmentServiceTest`

---

## 미구현 (예정)

| 도메인 | 비고 |
|---|---|
| Auth / JWT | Refresh Token Redis 저장 |
| User | 회원가입/탈퇴/정보 수정 |
| Hospital | 목록/상세/지도 검색 |
| Procedure | 시술 조회 (Equipment 연계) |
| Bookmark | Auth 구현 후 개발 |
| Schedule | 일정 CRUD + 알림 |
| Admin | 어드민 기능 (후순위) |

---

## 논의 중 / 미결 사항

### tb_article content 필드 방식 → 결정 완료
- `content_url VARCHAR(255)` 제거 → `content MEDIUMTEXT` 로 전환
- 어드민에서 리치 에디터(TipTap, Quill 등)로 HTML 입력
- 앱 클라이언트는 WebView로 렌더링
- 영향 파일: `Article.java`, `ArticleResponse.java`, `ArticleMapper.java`, `ArticleApiSpecification.java`, 테스트 2종, DDL, 명세서

---

## 응답 구조

### 성공 응답
```json
{
  "success": true,
  "data": { ... }
}
```

### 실패 응답
```json
{
  "success": false,
  "errorCode": "H002",
  "message": "기기를 찾을 수 없습니다."
}
```

> `@JsonInclude(NON_NULL)` 적용 — null 필드 자동 미노출