# 병원 지도 API 설계 문서

---

## 0. 쉽게 이해하는 지도 API 동작 구조

지도 API는 크게 **"지도를 보여주는 것"** 과 **"병원 정보를 보여주는 것"** 두 가지로 나뉩니다.

### 줌 레벨에 따라 보이는 것이 달라진다

지도를 축소하면 넓은 지역을 보게 되고, 확대하면 좁은 지역을 자세히 보게 됩니다.
이때 **병원이 얼마나 모여서 보이느냐**가 달라집니다.

```
[많이 축소]                [중간]                  [많이 확대]

  서울 ●142          강남구 ●23   서초구 ●8        📍 📍 📍
  경기 ●89           마포구 ●11                    📍    📍
  인천 ●34

  → 시/도 단위로 묶임     → 시/군/구 단위로 묶임    → 개별 병원 핀
```

### 클러스터(숫자 버블)를 클릭하면?

줌인이 되는 것이 아닙니다.
**그 구역에 속한 병원 목록이 하단 시트(Bottom Sheet)로 올라옵니다.**

```
  강남구 ●23  ← 클릭
       ↓
  ┌──────────────────────────┐
  │ 강남구 병원 23개           │  ← 하단 시트
  │ ───────────────────────  │
  │ 📍 강남 제모 클리닉        │
  │    서울 강남구 역삼동       │
  │ ───────────────────────  │
  │ 📍 역삼 스킨케어           │
  │    서울 강남구 역삼동       │
  └──────────────────────────┘
```

### 개별 마커(핀) 또는 목록 아이템을 클릭하면?

병원 상세 정보 화면으로 이동합니다.

```
  📍 클릭  또는  목록에서 병원 클릭
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
| 2 | `GET /api/v1/hospitals` | 클러스터 숫자 클릭 시 |
| 3 | `GET /api/v1/hospitals/{hospitalId}` | 마커 클릭 or 목록 아이템 클릭 시 |

---

## 2. API 상세

---

### API 1 — 지도 렌더링용 데이터 조회

지도가 보여주는 화면 영역(뷰포트)과 현재 줌 레벨을 받아서,
클러스터 또는 개별 마커 데이터를 반환합니다.

```
GET /api/v1/hospitals/map
  ?swLat=37.48&swLng=126.98    ← 지도 화면 남서(좌하단) 모서리 좌표
  &neLat=37.56&neLng=127.08    ← 지도 화면 북동(우상단) 모서리 좌표
  &zoomLevel=12                ← 현재 줌 레벨
```

#### 줌 레벨 → 응답 타입 분기

| zoomLevel | 응답 type | 클러스터 단위 | GROUP BY 기준 |
|---|---|---|---|
| 1 ~ 9 | `CLUSTER` | 시/도 (SIDO) | `sido_name` |
| 10 ~ 12 | `CLUSTER` | 시/군/구 (SIGUNGU) | `sigungu_name` |
| 13 ~ 14 | `CLUSTER` | 읍/면/동 (DONG) | `dong_name` |
| 15 ~ | `MARKER` | 개별 병원 핀 | - |

> 줌 레벨 경계값은 사용하는 지도 SDK (Naver Map / Kakao Map)에 따라 조정

#### 응답 — CLUSTER 타입

클러스터를 클릭했을 때 목록 API를 호출하기 위해 `level`, `name`, `parent` 를 함께 반환합니다.

```json
{
  "success": true,
  "data": {
    "type": "CLUSTER",
    "items": [
      {
        "count": 23,
        "lat": 37.517,
        "lng": 127.047,
        "level": "SIGUNGU",
        "name": "강남구",
        "parent": "서울특별시"
      },
      {
        "count": 8,
        "lat": 37.483,
        "lng": 127.032,
        "level": "SIGUNGU",
        "name": "서초구",
        "parent": "서울특별시"
      }
    ]
  }
}
```

#### 응답 — MARKER 타입

마커 클릭 시 상세 API를 호출하기 위해 `hospitalId` 를 포함합니다.
상세 정보(주소, 전화번호 등)는 담지 않습니다.

```json
{
  "success": true,
  "data": {
    "type": "MARKER",
    "items": [
      { "hospitalId": 101, "name": "강남 제모 클리닉", "lat": 37.512, "lng": 127.059 },
      { "hospitalId": 102, "name": "역삼 스킨케어",    "lat": 37.500, "lng": 127.041 }
    ]
  }
}
```

---

### API 2 — 클러스터 내 병원 목록 조회

클러스터를 클릭했을 때 하단 시트에 표시할 병원 목록을 반환합니다.
API 1의 클러스터 응답에 담긴 `level`, `name`, `parent` 값을 그대로 파라미터로 넘깁니다.

```
GET /api/v1/hospitals
  ?level=SIGUNGU          ← API 1 클러스터 응답의 level 값
  &name=강남구             ← API 1 클러스터 응답의 name 값
  &parent=서울특별시        ← API 1 클러스터 응답의 parent 값
```

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

마커 클릭 또는 목록 아이템 클릭 시 병원의 전체 상세 정보를 반환합니다.

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
┌─────────────────────────────────────────────────────────┐
│  STEP 1. 지도 화면 진입 or 이동/줌 변경 시마다              │
│                                                         │
│  GET /api/v1/hospitals/map                              │
│    ?swLat=...&swLng=...&neLat=...&neLng=...&zoomLevel=12│
└────────────────────────┬────────────────────────────────┘
                         │
            ┌────────────┴────────────┐
            │ type = CLUSTER          │ type = MARKER
            ▼                         ▼
    지도에 숫자 버블 렌더링         지도에 핀 아이콘 렌더링
    (count, lat, lng)             (hospitalId, name, lat, lng)
            │                         │
            │ 숫자 버블 클릭            │ 핀 클릭
            ▼                         ▼
┌───────────────────────┐   ┌──────────────────────────┐
│  STEP 2.              │   │  STEP 3.                 │
│  GET /api/v1/hospitals│   │  GET /api/v1/hospitals   │
│    ?level=SIGUNGU     │   │      /{hospitalId}       │
│    &name=강남구        │   └────────────┬─────────────┘
│    &parent=서울특별시  │                │
└───────────┬───────────┘                ▼
            │                    병원 상세 화면 표시
            ▼
    하단 시트에 목록 표시
    (hospitalId, name, address, tags ...)
            │
            │ 목록 아이템 클릭
            ▼
┌───────────────────────────┐
│  STEP 3.                  │
│  GET /api/v1/hospitals    │
│      /{hospitalId}        │
└───────────┬───────────────┘
            │
            ▼
    병원 상세 화면 표시
```

---

## 4. 서버 쿼리 전략

| API | 사용 테이블 | 핵심 쿼리 전략 |
|---|---|---|
| map (CLUSTER) | `tb_hospital` + `tb_district` | `MBRContains`로 뷰포트 필터 → `GROUP BY` 행정구역명 → `tb_district` JOIN으로 중심 좌표 획득 |
| map (MARKER) | `tb_hospital` | `MBRContains`로 뷰포트 필터 → 개별 병원 좌표 목록 반환 (LIMIT 적용) |
| list | `tb_hospital` + `tb_hospital_tag` | `sido_name` / `sigungu_name` / `dong_name` 컬럼 필터 |
| detail | `tb_hospital` + `tb_hospital_tag` + `tb_hospital_equipment` | `hospital_id` 단건 조회 |

### CLUSTER 모드 쿼리 예시 (SIGUNGU 단위)

```sql
SELECT h.sigungu_name,
       d.center_lat,
       d.center_lng,
       COUNT(*)        AS count
FROM tb_hospital h
JOIN tb_district d
  ON d.level       = 'SIGUNGU'
 AND d.name        = h.sigungu_name
 AND d.parent_name = h.sido_name
WHERE MBRContains(
    ST_GeomFromText('POLYGON((swLng swLat, neLng swLat, neLng neLat, swLng neLat, swLng swLat))', 4326),
    h.location
)
GROUP BY h.sigungu_name, d.center_lat, d.center_lng
ORDER BY count DESC;
```

### MARKER 모드 쿼리 예시

```sql
SELECT hospital_id,
       name,
       ST_Y(location) AS lat,
       ST_X(location) AS lng
FROM tb_hospital
WHERE MBRContains(
    ST_GeomFromText('POLYGON((swLng swLat, neLng swLat, neLng neLat, swLng neLat, swLng swLat))', 4326),
    location
)
LIMIT 100;
```

---

## 5. 주요 설계 결정 사항

| 항목 | 결정 | 이유 |
|---|---|---|
| 클러스터 방식 | 서버사이드 행정구역 기반 | 줌 레벨별 GROUP BY로 단순하고 빠름 |
| 마커 겹침 처리 | 클라이언트 픽셀 기반 재클러스터링 | 겹침 여부는 픽셀 거리 기준이라 서버가 판단 불가 |
| 중심 좌표 저장 | `tb_district` 별도 테이블 분리 | 병원 행마다 같은 좌표를 중복 저장하는 낭비 방지 |
| 클러스터 클릭 동작 | 줌인 없이 하단 시트로 목록 표시 | Naver Land 방식, 줌인은 사용자가 직접 제어 |
| MARKER 응답 LIMIT | 100건 | 뷰포트가 넓을 때 과부하 방지 |