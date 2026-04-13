"""
2단계: 좌표 없는 병원 주소 → 위경도 변환 (Kakao Local API)
- 입력: raw_hospitals.csv
- 결과: hospitals_with_coords.csv
"""

import os
import time
import requests
import pandas as pd
from dotenv import load_dotenv

load_dotenv()

KAKAO_KEY = os.getenv("KAKAO_REST_API_KEY")
KAKAO_URL = "https://dapi.kakao.com/v2/local/search/address.json"


def geocode(address: str) -> tuple[float | None, float | None]:
    """주소 문자열 → (위도, 경도) 반환. 실패 시 (None, None)"""
    headers = {"Authorization": f"KakaoAK {KAKAO_KEY}"}
    params = {"query": address, "analyze_type": "similar"}
    try:
        resp = requests.get(KAKAO_URL, headers=headers, params=params, timeout=5)
        resp.raise_for_status()
        docs = resp.json().get("documents", [])
        if docs:
            return float(docs[0]["y"]), float(docs[0]["x"])  # y=위도, x=경도
    except Exception as e:
        print(f"  Geocoding 실패 ({address}): {e}")
    return None, None


def main():
    script_dir = os.path.dirname(__file__)
    input_path = os.path.join(script_dir, "raw_hospitals.csv")
    output_path = os.path.join(script_dir, "hospitals_with_coords.csv")

    if not os.path.exists(input_path):
        print(f"파일 없음: {input_path}\ncollect_hospitals.py 를 먼저 실행하세요.")
        return

    df = pd.read_csv(input_path, dtype=str)
    # 심평원 API 응답 필드명: XPos(경도/longitude), YPos(위도/latitude) — 대문자 P
    df["XPos"] = pd.to_numeric(df.get("XPos", None), errors="coerce")
    df["YPos"] = pd.to_numeric(df.get("YPos", None), errors="coerce")

    # 좌표 누락 행만 Geocoding
    needs_geocode = df["XPos"].isna() | df["YPos"].isna()
    total = needs_geocode.sum()
    print(f"전체 {len(df)}건 중 좌표 누락 {total}건 → Geocoding 진행")

    success = 0
    fail_list = []

    for idx, row in df[needs_geocode].iterrows():
        addr = row.get("addr", "")
        if not addr or pd.isna(addr):
            fail_list.append(idx)
            continue

        lat, lng = geocode(addr)
        if lat is not None:
            df.at[idx, "YPos"] = lat  # YPos = 위도
            df.at[idx, "XPos"] = lng  # XPos = 경도
            success += 1
        else:
            fail_list.append(idx)

        if (success + len(fail_list)) % 50 == 0:
            print(f"  진행: {success + len(fail_list)}/{total} (성공 {success} / 실패 {len(fail_list)})")

        time.sleep(0.05)  # Kakao API rate limit 방지

    print(f"\nGeocoding 완료: 성공 {success}건 / 실패 {len(fail_list)}건")

    # 좌표 없는 행은 DB INSERT 불가 → 별도 컬럼으로 표시
    df["coord_ok"] = (~(df["XPos"].isna() | df["YPos"].isna())).astype(int)

    df.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"저장 완료: {output_path}")
    print(f"DB 적재 가능: {df['coord_ok'].sum()}건 / 전체 {len(df)}건")

    # 좌표 미확보 병원 별도 파일로 저장
    if fail_list:
        fail_cols = ["ykiho", "yadmNm", "addr", "telno", "clCdNm", "dgsbjtCdNm"]
        existing_fail_cols = [c for c in fail_cols if c in df.columns]
        fail_df = df.loc[fail_list, existing_fail_cols].copy()
        fail_df["실패사유"] = "Geocoding 실패 (주소로 좌표 변환 불가)"

        fail_path = os.path.join(script_dir, "geocode_failed.csv")
        fail_df.to_csv(fail_path, index=False, encoding="utf-8-sig")
        print(f"\n좌표 미확보 병원 목록 저장: {fail_path} ({len(fail_df)}건)")


if __name__ == "__main__":
    main()