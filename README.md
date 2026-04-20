# HEHE Backend

_last update : 26.04.20_

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

## 실행 환경

- MySQL 데이터베이스: `hehe_db`
- 기본 포트: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`


## 접속 방법
```

# SSH 접속
ssh -i your-key.pem ec2-user@{ELASTIC_IP}

# Swagger UI
http://{ELASTIC_IP}:8080/swagger-ui.html

# API 호출 예시
http://{ELASTIC_IP}:8080/api/v1/common/time
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

### Upload
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/v1/upload/image` | S3 이미지 업로드 |

---

## 미구현 (예정)

- Auth / JWT (소셜 로그인, Refresh Token)
- User (회원가입 · 탈퇴 · 정보 수정)
- Bookmark (병원 찜하기)
- FCM 푸시 알림