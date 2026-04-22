# HEHE Backend

_last update : 26.04.22_ (문의 내역 조회 API 추가)

레이저 제모 병원 찾기 · 예약 앱 백엔드 서버

---

## 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 17 |
| Spring Boot | 3.5 |
| MyBatis | 3.0.3 |
| MySQL | 8 |
| Redis | - |
| Springdoc OpenAPI (Swagger) | 2.8.0 |

---

## 실행 환경 (로컬)

- MySQL 데이터베이스: `hehe_db`
- 기본 포트: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`


## 접속 방법
```

# Swagger UI (운영)
https://api.hehehe.kr/swagger-ui.html

# Swagger UI (로컬)
http://localhost:8080/swagger-ui.html

# API 호출 예시
https://api.hehehe.kr/api/v1/common/time
```

## 도메인
| 도메인 | 용도 |
|---|---|
| `hehehe.kr` | 메인 도메인 |
| `www.hehehe.kr` | www 서브도메인 |
| `api.hehehe.kr` | API 서버 |

## 인프라 구성
```
클라이언트
  ↓ HTTPS (443)
Route 53 (hehehe.kr)
  ↓
ALB (hehe-alb) — ACM 인증서 적용
  ↓ HTTP (8080)
EC2 t3.micro (ap-northeast-2c)
  ├── Spring Boot (Docker)
  ├── MySQL 8
  └── Redis
```


---

## 구현된 API

### Common
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/common/time` | 서버 현재 시각 조회 |

### Article
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/articles` | 아티클 목록 조회 |
| GET | `/api/v1/articles/{articleId}` | 아티클 단건 조회 |

### Equipment
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/equipments/main` | 홈 노출 기기 목록 조회 |
| GET | `/api/v1/equipments/{equipId}` | 기기 단건 조회 |

### Schedule
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/schedules/summary` | 전체 일정 날짜별 건수 조회 (캘린더 점 표시용) |
| GET | `/api/v1/schedules/upcoming` | 예정 일정 N건 조회 |
| GET | `/api/v1/schedules/daily` | 날짜별 예약 일정 조회 |
| POST | `/api/v1/schedules` | 예약 일정 생성 |
| GET | `/api/v1/schedules/{scheduleId}` | 예약 일정 단건 조회 |
| PATCH | `/api/v1/schedules/{scheduleId}` | 예약 일정 수정 |
| DELETE | `/api/v1/schedules/{scheduleId}` | 예약 일정 삭제 |
| POST | `/api/v1/schedules/{scheduleId}/alarms` | 알림 등록 |
| DELETE | `/api/v1/schedules/{scheduleId}/alarms/{alarmType}` | 알림 삭제 |

### Hospital
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/hospitals/map` | 지도 뷰포트 내 클러스터 조회 |
| GET | `/api/v1/hospitals` | 클러스터 내 병원 목록 조회 |
| GET | `/api/v1/hospitals/{hospitalId}` | 병원 상세 조회 |

### User
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/users/summary` | 마이페이지 요약 조회 (JWT 필요) |

### Bookmark
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/bookmarks` | 찜한 병원 목록 조회 (JWT 필요) |
| POST | `/api/v1/bookmarks/{hospitalId}` | 찜 추가 (JWT 필요) |
| DELETE | `/api/v1/bookmarks/{hospitalId}` | 찜 삭제 (JWT 필요) |

### RecentView
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/recent-views` | 최근 본 병원 목록 조회 (JWT 필요) |
| POST | `/api/v1/recent-views/{hospitalId}` | 최근 본 병원 기록 (JWT 필요) |

### Contact
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/v1/contacts` | 문의 내역 목록 조회 (JWT 필요) |
| POST | `/api/v1/contacts` | 문의 내역 저장 (JWT 필요) |
| DELETE | `/api/v1/contacts/{contactId}` | 문의 내역 삭제 (JWT 필요) |

### Upload
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/v1/upload/image` | S3 이미지 업로드 |

---

## 미구현 (예정)

- Auth / JWT (소셜 로그인, Refresh Token)
- User (회원가입 · 탈퇴 · 정보 수정)
- FCM 푸시 알림