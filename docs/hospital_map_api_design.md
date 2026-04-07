# 병원 지도 API 설계 문서

---

## 0. 쉽게 이해하는 지도 API 동작 구조

### 핵심 원칙

- 지도에는 항상 **숫자 버블**만 표시됩니다.
- 줌 레벨에 따라 **좌표 반올림 정밀도**만 달라집니다. 응답 구조는 항상 동일합니다.
- 버블에는 숫자만 표시되며, 지역 이름은 표시하지 않습니다.

### 줌 레벨에 따라 묶이는 범위가 달라짐

```
[많이 축소]                [중간]                    [많이 확대]

    ●142                ●23      ●8               ●3    ●1
    ●89                 ●11                        ●1       ●2
    ●34

  넓은 범위로 묶임         중간 범위로 묶임            좁은 범위로 묶임
  (precision=1, ~11km)  (precision=2, ~1km)      (precision=4, ~10m)
```

### 숫자 버블을 클릭하면?

줌 레벨과 무관하게 동일하게 동작합니다.
**그 위치에 속한 병원 목록이 하단 시트로 올라옵니다.**

```
  ●23  ← 클릭          또는          ●3  ← 클릭 (최대 확대)
     ↓                                   ↓
  ┌──────────────────────────┐    ┌──────────────────────────┐
  │ 병원 23개                 │    │ 이 위치 병원 3개           │
  │ ───────────────────────  │    │ ───────────────────────  │
  │ 강남 제모 클리닉           │    │ 강남 제모 클리닉           │
  │ 역삼 스킨케어              │    │ 역삼 피부과               │
  │ ...                      │    │ 강남성형외과               │
  └──────────────────────────┘    └──────────────────────────┘
```

### 목록에서 병원을 클릭하면?

```
  목록에서 병원 클릭
       ↓
  ┌──────────────────────────┐
  │ 강남 제모 클리닉           │  ← 병원 상세
  │ 서울 강남구 역삼동 123-4   │
  │ 02-1234-5678             │
  │ 젠틀맥스프로 2대           │
  │ #여성원장 #주차가능         │
  └──────────────────────────┘
```

---

## 1. API 목록

| # | API | 호출 시점 |
|---|---|---|
| 1 | `GET /api/v1/hospitals/map` | 지도 화면 진입 / 이동 / 줌 변경 시마다 |
| 2 | `GET /api/v1/hospitals` | 숫자 버블 클릭 시 |
| 3 | `GET /api/v1/hospitals/{hospitalId}` | 목록 아이템 클릭 시 |

---

## 2. API 상세

---

### API 1 — 지도 렌더링용 클러스터 데이터 조회

뷰포트 영역과 줌 레벨을 받아 클러스터 목록을 반환합니다.
줌 레벨에 따라 좌표 반올림 정밀도(`precision`)가 달라지며, 응답 구조는 항상 동일합니다.

```
GET /api/v1/hospitals/map
  ?swLat=37.48&swLng=126.98    ← 지도 화면 남서(좌하단) 모서리 좌표
  &neLat=37.56&neLng=127.08    ← 지도 화면 북동(우상단) 모서리 좌표
  &zoomLevel=12                ← 현재 줌 레벨
```

#### 줌 레벨 → 좌표 정밀도 매핑

| zoomLevel | precision | 묶음 범위 |
|---|---|---|
| 1 ~ 9 | 1 | 약 11km |
| 10 ~ 12 | 2 | 약 1km |
| 13 ~ 14 | 3 | 약 100m |
| 15 ~ | 4 | 약 10m |

> 줌 레벨 경계값은 사용하는 지도 SDK (Naver Map / Kakao Map)에 따라 조정

#### 응답

```json
{
  "success": true,
  "data": {
    "precision": 2,
    "items": [
      { "count": 23, "lat": 37.52, "lng": 127.05 },
      { "count": 8,  "lat": 37.48, "lng": 127.03 },
      { "count": 1,  "lat": 37.51, "lng": 126.99 }
    ]
  }
}
```

- `precision`: 클라이언트가 목록 API 호출 시 그대로 전달하는 값
- `lat`, `lng`: 반올림된 좌표 (클러스터 핀 위치 + 목록 API 식별자)

---

### API 2 — 클러스터 내 병원 목록 조회

버블을 클릭했을 때 하단 시트에 표시할 병원 목록을 반환합니다.
API 1 응답의 `lat`, `lng`, `precision` 을 그대로 넘깁니다.

```
GET /api/v1/hospitals
  ?lat=37.52          ← API 1 응답의 lat 그대로
  &lng=127.05         ← API 1 응답의 lng 그대로
  &precision=2        ← API 1 응답의 precision 그대로
```

서버에서 `ROUND(ST_Y(location), precision) = lat AND ROUND(ST_X(location), precision) = lng` 조건으로 조회합니다.

#### 응답

```json
{
  "success": true,
  "data": [
    {
      "hospitalId": 101,
      "name": "강남 제모 클리닉",
      "address": "서울 강남구 역삼동 123-4",
      "thumbnailUrl": "https://...",
      "tags": ["여성원장", "주차가능"]
    },
    {
      "hospitalId": 102,
      "name": "역삼 스킨케어",
      "address": "서울 강남구 역삼동 56-7",
      "thumbnailUrl": "https://...",
      "tags": ["야간진료"]
    }
  ]
}
```

---

### API 3 — 병원 상세 조회

목록 아이템 클릭 시 병원의 전체 상세 정보를 반환합니다.

```
GET /api/v1/hospitals/{hospitalId}
```

#### 응답

```json
{
  "success": true,
  "data": {
    "hospitalId": 101,
    "name": "강남 제모 클리닉",
    "address": "서울 강남구 역삼동 123-4",
    "lat": 37.512,
    "lng": 127.059,
    "contactNumber": "02-1234-5678",
    "contactUrl": "https://...",
    "tags": ["여성원장", "주차가능"],
    "equipments": [
      { "modelName": "젠틀맥스프로", "count": 2 }
    ]
  }
}
```

---

## 3. 전체 사용 흐름

```
┌──────────────────────────────────────────────────────────┐
│  STEP 1. 지도 화면 진입 or 이동/줌 변경 시마다               │
│                                                          │
│  GET /api/v1/hospitals/map                               │
│    ?swLat=...&swLng=...&neLat=...&neLng=...&zoomLevel=12 │
│                                                          │
│  → 항상 숫자 클러스터 반환 { count, lat, lng }              │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼
                  지도에 숫자 버블 렌더링
                  (모든 줌 레벨 동일한 구조)
                           │
                           │ 숫자 버블 클릭
                           ▼
┌──────────────────────────────────────────────────────────┐
│  STEP 2.                                                 │
│  GET /api/v1/hospitals                                   │
│    ?lat={API1.lat}&lng={API1.lng}&precision={API1.prec}  │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼
                  하단 시트에 병원 목록 표시
                  (hospitalId, name, address, tags ...)
                           │
                           │ 목록 아이템 클릭
                           ▼
┌──────────────────────────────────────────────────────────┐
│  STEP 3.                                                 │
│  GET /api/v1/hospitals/{hospitalId}                      │
└──────────────────────────┬───────────────────────────────┘
                           │
                           ▼
                  병원 상세 화면 표시
```

---

## 4. 서버 쿼리 전략

| API | 사용 테이블 | 핵심 쿼리 전략 |
|---|---|---|
| map | `tb_hospital` | `MBRContains`로 뷰포트 필터 → `GROUP BY ROUND(lat, p), ROUND(lng, p)` |
| list | `tb_hospital` + `tb_hospital_tag` | `ROUND(ST_Y(location), p) = lat AND ROUND(ST_X(location), p) = lng` |
| detail | `tb_hospital` + `tb_hospital_tag` + `tb_hospital_equipment` | `hospital_id` 단건 조회 |

### 클러스터 쿼리 예시

```sql
SELECT ROUND(ST_Y(location), #{precision}) AS lat,
       ROUND(ST_X(location), #{precision}) AS lng,
       COUNT(*)                             AS count
FROM tb_hospital
WHERE MBRContains(
    ST_GeomFromText('POLYGON((swLng swLat, neLng swLat, neLng neLat, swLng neLat, swLng swLat))', 4326),
    location
)
GROUP BY ROUND(ST_Y(location), #{precision}),
         ROUND(ST_X(location), #{precision})
ORDER BY count DESC;
```

### 목록 쿼리 예시

```sql
SELECT h.hospital_id, h.name, h.address, h.contact_number, h.contact_url,
       GROUP_CONCAT(t.tag_name) AS tags
FROM tb_hospital h
LEFT JOIN tb_hospital_tag t ON t.hospital_id = h.hospital_id
WHERE ROUND(ST_Y(h.location), #{precision}) = #{lat}
  AND ROUND(ST_X(h.location), #{precision}) = #{lng}
GROUP BY h.hospital_id;
```

---

## 5. 주요 설계 결정 사항

| 항목 | 결정 | 이유 |
|---|---|---|
| 렌더링 방식 | 항상 숫자 클러스터, 핀(마커) 없음 | 같은 건물에 병원 여러 개인 경우 핀으로는 표현 불가 |
| 클러스터 타입 구분 | 단일 방식 (좌표 반올림) | 버블에 지역 이름 표시 계획 없음 → DISTRICT/LOCATION 구분 불필요 |
| 클러스터링 기준 | `ROUND(lat, precision), ROUND(lng, precision)` | 구현 단순, tb_district 테이블 불필요 |
| 버블 클릭 동작 | 줌인 없이 하단 시트로 목록 표시 | 줌인은 사용자가 직접 제어 |
| 목록 API 식별자 | `lat`, `lng`, `precision` | API 1 응답값을 그대로 전달하는 단순한 구조 |
| tb_district 테이블 | 미사용 (제거) | 좌표 반올림 방식으로 대체되어 불필요 |

> **향후 버블에 지역명(강남구, 역삼동 등) 표시가 필요해지면**
> `tb_district` 테이블과 `sido_name`, `sigungu_name`, `dong_name` 컬럼 추가 및 DISTRICT 클러스터 타입 도입 검토