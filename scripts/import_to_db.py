"""
3단계: hospitals_with_coords.csv → tb_hospital INSERT
- 좌표가 확보된 병원만 INSERT (coord_ok=1)
- hospital_id: timestamp 기반 생성 (밀리초 + 인덱스로 유니크 보장)
- 중복 실행 방지: ykiho 기준으로 이미 존재하면 SKIP (ON DUPLICATE KEY 미사용, 사전 조회 방식)

실행 방법:
  python import_to_db.py            # 콘솔 미리보기만 (DB 미삽입)
  python import_to_db.py --execute  # 실제 DB INSERT 실행
"""

import os
import sys
import time
import pandas as pd
import pymysql
from dotenv import load_dotenv

load_dotenv()

# --execute 플래그가 없으면 콘솔 출력만 하는 드라이런 모드
DRY_RUN = "--execute" not in sys.argv

DB_CONFIG = {
    "host":   os.getenv("DB_HOST", "localhost"),
    "port":   int(os.getenv("DB_PORT", 3306)),
    "db":     os.getenv("DB_NAME", "hehe_db"),
    "user":   os.getenv("DB_USER", "root"),
    "passwd": os.getenv("DB_PASSWORD", ""),
    "charset": "utf8mb4",
}

INSERT_SQL = """
INSERT INTO tb_hospital (hospital_id, name, address, location, contact_number, contact_url)
VALUES (%s, %s, %s, ST_GeomFromText(%s, 4326), %s, %s)
"""

# tb_hospital에 ykiho 컬럼이 없으므로 name+address 조합으로 중복 체크
CHECK_SQL = "SELECT COUNT(*) FROM tb_hospital WHERE name = %s AND address = %s"

# 드라이런 콘솔 출력 시 표시할 최대 건수
DRY_RUN_PREVIEW_LIMIT = 20


def make_wkt(lat: float, lng: float) -> str:
    """위도/경도 → WKT POINT 문자열 (SRID 4326: POINT(위도 경도))"""
    return f"POINT({lat} {lng})"


def run_dry(df: pd.DataFrame) -> None:
    """DB 연결 없이 INSERT 예정 데이터를 콘솔에 출력"""
    print("\n" + "=" * 70)
    print(f"[DRY RUN] 실제 INSERT 없음 — 콘솔 미리보기 모드")
    print(f"실제 INSERT 하려면: python import_to_db.py --execute")
    print("=" * 70)

    valid_rows = []
    skipped = 0

    for _, row in df.iterrows():
        name    = str(row.get("yadmNm", "")).strip()
        address = str(row.get("addr", "")).strip()
        lat     = row["YPos"]
        lng     = row["XPos"]
        tel     = str(row.get("telno", "")).strip() or "정보없음"
        url     = str(row.get("hospUrl", "")).strip() or None
        clcd_nm = str(row.get("clCdNm", "")).strip()
        dgsbjt  = str(row.get("dgsbjtCdNm", "")).strip()

        if not name or not address:
            skipped += 1
            continue

        valid_rows.append({
            "병원명":       name,
            "주소":         address,
            "전화번호":     tel,
            "위도(YPos)":   lat,
            "경도(XPos)":   lng,
            "POINT(WKT)":   make_wkt(lat, lng),
            "종별":         clcd_nm,
            "진료과목":     dgsbjt,
            "홈페이지":     url or "(없음)",
        })

    total = len(valid_rows)
    print(f"\n총 INSERT 예정: {total}건 / 스킵(이름·주소 누락): {skipped}건")

    preview_count = min(total, DRY_RUN_PREVIEW_LIMIT)
    print(f"\n--- 상위 {preview_count}건 미리보기 ---\n")

    for i, r in enumerate(valid_rows[:preview_count], 1):
        print(f"[{i:>3}] {r['병원명']}")
        print(f"       주소     : {r['주소']}")
        print(f"       전화번호 : {r['전화번호']}")
        print(f"       좌표     : {r['POINT(WKT)']}")
        print(f"       종별/과목: {r['종별']} / {r['진료과목']}")
        print(f"       홈페이지 : {r['홈페이지']}")
        print()

    if total > DRY_RUN_PREVIEW_LIMIT:
        print(f"... 외 {total - DRY_RUN_PREVIEW_LIMIT}건 (전체 확인은 hospitals_with_coords.csv 참조)")

    # 진료과목·종별 분포 요약
    print("\n--- 종별 분포 ---")
    if "clCdNm" in df.columns:
        print(df["clCdNm"].value_counts().to_string())

    print("\n--- 진료과목 분포 ---")
    if "dgsbjtCdNm" in df.columns:
        print(df["dgsbjtCdNm"].value_counts().to_string())

    print("\n--- 구별 분포 ---")
    if "sgguCdNm" in df.columns:
        print(df["sgguCdNm"].value_counts().to_string())

    print("\n[DRY RUN 완료] 데이터가 올바르면 --execute 플래그로 재실행하세요.")


def run_insert(df: pd.DataFrame) -> None:
    """실제 DB INSERT 실행"""
    print("\n" + "=" * 70)
    print("[EXECUTE] 실제 DB INSERT 시작")
    print("=" * 70)

    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()

    inserted = 0
    skipped = 0
    error_rows = []
    base_time = int(time.time() * 1000)

    try:
        for i, row in df.iterrows():
            name    = str(row.get("yadmNm", "")).strip()
            address = str(row.get("addr", "")).strip()
            lat     = row["YPos"]    # YPos = 위도 (latitude)
            lng     = row["XPos"]    # XPos = 경도 (longitude)
            tel     = str(row.get("telno", "")).strip() or "정보없음"
            url     = str(row.get("hospUrl", "")).strip() or None  # 병원 홈페이지 URL

            if not name or not address:
                skipped += 1
                continue

            # 중복 체크
            cursor.execute(CHECK_SQL, (name, address))
            if cursor.fetchone()[0] > 0:
                skipped += 1
                continue

            hospital_id = base_time + i  # 유니크 보장
            wkt = make_wkt(lat, lng)

            try:
                cursor.execute(INSERT_SQL, (hospital_id, name, address, wkt, tel, url))
                inserted += 1
            except Exception as e:
                error_rows.append({
                    "yadmNm": name,
                    "addr": address,
                    "telno": tel,
                    "실패사유": str(e),
                })

            if (inserted + skipped) % 100 == 0:
                conn.commit()
                print(f"  진행: {inserted + skipped}/{len(df)} (삽입 {inserted} / 스킵 {skipped})")

        conn.commit()

    finally:
        cursor.close()
        conn.close()

    print(f"\n완료: 삽입 {inserted}건 / 스킵(중복) {skipped}건 / 오류 {len(error_rows)}건")
    print("DB에서 확인: SELECT COUNT(*) FROM tb_hospital;")

    if error_rows:
        script_dir = os.path.dirname(__file__)
        fail_path = os.path.join(script_dir, "import_failed.csv")
        pd.DataFrame(error_rows).to_csv(fail_path, index=False, encoding="utf-8-sig")
        print(f"INSERT 실패 목록 저장: {fail_path} ({len(error_rows)}건)")


def main():
    script_dir = os.path.dirname(__file__)
    input_path = os.path.join(script_dir, "hospitals_with_coords.csv")

    if not os.path.exists(input_path):
        print(f"파일 없음: {input_path}\ngeocde_hospitals.py 를 먼저 실행하세요.")
        return

    df = pd.read_csv(input_path, dtype=str)
    # 심평원 API 응답 필드명: XPos(경도/longitude), YPos(위도/latitude) — 대문자 P
    df["XPos"] = pd.to_numeric(df["XPos"], errors="coerce")
    df["YPos"] = pd.to_numeric(df["YPos"], errors="coerce")
    df["coord_ok"] = pd.to_numeric(df.get("coord_ok", 1), errors="coerce").fillna(0)

    # 좌표 확보된 행만 처리
    df = df[df["coord_ok"] == 1].reset_index(drop=True)
    print(f"좌표 확보된 병원: {len(df)}건")

    # 종별코드 필터링: 의원(31), 병원(21), 종합병원(11)만 포함
    # 상급종합(01), 요양병원(28), 한방병원(92), 치과병원(41) 등 제외
    ALLOWED_CL_CD = {"21", "31"}  # 의원(31), 병원(21) — 종합병원(11) 제외
    before_filter = len(df)
    df = df[df["clCd"].isin(ALLOWED_CL_CD)].reset_index(drop=True)
    print(f"종별 필터링: {before_filter}건 → {len(df)}건 (의원/병원/종합병원만 포함)")

    # 병원명 기반 노이즈 제외 필터
    # 피부과/성형외과와 무관한 과목명이 병원명에 포함된 경우 제외
    # 단, '피부'가 함께 포함된 경우는 유지 (예: "강남피부내과의원")
    EXCLUDE_KEYWORDS = [
        "내과", "안과", "이비인후과", "산부인과",
        "외과",       # 정형외과, 신경외과 등 (성형외과는 예외)
        "비뇨",       # 비뇨기과
        "신경",       # 신경과, 신경외과
        "정신건강", "치과",
        "한의원", "한방", "재활의학과", "가정의학", "응급의학과",
        "마취", "소아", "청소년", "여성", "성모", "아이들", "남성", "가족",
    ]
    exclude_pattern = "|".join(EXCLUDE_KEYWORDS)

    before_name_filter = len(df)
    is_noise = (
        df["yadmNm"].str.contains(exclude_pattern, na=False) &
        ~df["yadmNm"].str.contains("피부|성형외과", na=False)  # 피부/성형외과가 함께 있으면 유지
    )
    excluded_df = df[is_noise]
    df = df[~is_noise].reset_index(drop=True)
    print(f"병원명 필터링: {before_name_filter}건 → {len(df)}건 (노이즈 {before_name_filter - len(df)}건 제외)")

    if DRY_RUN:
        run_dry(df)
    else:
        run_insert(df)


if __name__ == "__main__":
    main()