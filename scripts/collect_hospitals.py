"""
1단계: 건강보험심사평가원 API에서 병원 데이터 수집
- 서울 피부과(D007) + 성형외과(D009) 병원 전체 수집
- 결과: raw_hospitals.csv
"""

import os
import time
import requests
import pandas as pd
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv("PUBLIC_DATA_API_KEY")
BASE_URL = "https://apis.data.go.kr/B551182/hospInfoServicev2/getHospBasisList"

# 수집 대상: 서울(110000) × 피부과(14) + 성형외과(08)
# dgsbjtCd 코드: 14=피부과, 08=성형외과 (공공데이터 심평원 API 기준)
TARGETS = [
    {"sidoCd": "110000", "dgsbjtCd": "14", "label": "서울_피부과"},
    {"sidoCd": "110000", "dgsbjtCd": "08", "label": "서울_성형외과"},
]

NUM_OF_ROWS = 1000  # 페이지당 최대 1000건


def fetch_page(sido_cd: str, dgsbjt_cd: str, page_no: int) -> dict:
    """심평원 API 단일 페이지 호출
    공공데이터포털은 serviceKey를 params dict가 아닌 URL에 직접 포함해야 정상 동작함
    """
    url = (
        f"{BASE_URL}?serviceKey={API_KEY}"
        f"&sidoCd={sido_cd}&dgsbjtCd={dgsbjt_cd}"
        f"&numOfRows={NUM_OF_ROWS}&pageNo={page_no}&_type=json"
    )
    resp = requests.get(url, timeout=10)
    resp.raise_for_status()
    return resp.json()


def fetch_all(sido_cd: str, dgsbjt_cd: str, label: str) -> list[dict]:
    """페이지네이션을 반복하며 전체 데이터 수집"""
    print(f"\n[{label}] 수집 시작...")

    # 1페이지로 totalCount 파악
    first = fetch_page(sido_cd, dgsbjt_cd, 1)
    body = first["response"]["body"]
    total_count = body["totalCount"]
    total_pages = -(-total_count // NUM_OF_ROWS)  # ceiling division
    print(f"  총 {total_count}건 / {total_pages}페이지")

    items = body["items"]["item"]
    if isinstance(items, dict):  # 단건이면 dict로 옴
        items = [items]
    all_items = list(items)

    for page in range(2, total_pages + 1):
        print(f"  페이지 {page}/{total_pages} 수집 중...", end="\r")
        data = fetch_page(sido_cd, dgsbjt_cd, page)
        page_items = data["response"]["body"]["items"]["item"]
        if isinstance(page_items, dict):
            page_items = [page_items]
        all_items.extend(page_items)
        time.sleep(0.2)  # API 부하 방지

    print(f"\n  [{label}] 수집 완료: {len(all_items)}건")
    return all_items


def main():
    all_hospitals = []

    for target in TARGETS:
        items = fetch_all(target["sidoCd"], target["dgsbjtCd"], target["label"])
        for item in items:
            item["_source_label"] = target["label"]
        all_hospitals.extend(items)

    df = pd.DataFrame(all_hospitals)

    # 필요한 컬럼만 추출
    # 응답 필드명: XPos(경도/longitude), YPos(위도/latitude) — 대문자 P (실제 API 응답 기준)
    keep_cols = [
        "ykiho",      # 요양기관기호 (고유 ID)
        "yadmNm",     # 병원명
        "addr",       # 주소
        "telno",      # 전화번호
        "XPos",       # 경도 (longitude) — DB 적재 시 POINT(YPos XPos) 순서 주의
        "YPos",       # 위도 (latitude)
        "clCd",       # 종별코드 (31=의원, 21=병원 등)
        "clCdNm",     # 종별코드명
        "dgsbjtCd",   # 진료과목코드 (14=피부과, 08=성형외과)
        "dgsbjtCdNm", # 진료과목명
        "hospUrl",    # 병원 홈페이지 URL → tb_hospital.contact_url 에 활용
        "_source_label",
    ]
    existing_cols = [c for c in keep_cols if c in df.columns]
    df = df[existing_cols]

    # 중복 제거 (동일 ykiho — 피부과+성형외과 둘 다 해당되는 병원)
    before = len(df)
    df = df.drop_duplicates(subset=["ykiho"])
    print(f"\n중복 제거: {before}건 → {len(df)}건")

    output_path = os.path.join(os.path.dirname(__file__), "raw_hospitals.csv")
    df.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"저장 완료: {output_path}")
    print(f"\n[결과 미리보기]")
    print(df.head(3).to_string())

    # 좌표 누락 현황 출력
    if "XPos" in df.columns and "YPos" in df.columns:
        no_coord = df[df["XPos"].isna() | (df["XPos"] == "") | (df["XPos"] == 0)].shape[0]
        print(f"\n좌표 누락: {no_coord}건 (geocode_hospitals.py 로 처리 예정)")


if __name__ == "__main__":
    main()