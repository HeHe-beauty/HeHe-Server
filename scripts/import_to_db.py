"""
3단계: hospitals_with_coords.csv → tb_hospital INSERT
- 좌표가 확보된 병원만 INSERT (coord_ok=1)
- hospital_id: timestamp 기반 생성 (밀리초 + 인덱스로 유니크 보장)
- 중복 실행 방지: ykiho 기준으로 이미 존재하면 SKIP (ON DUPLICATE KEY 미사용, 사전 조회 방식)
"""

import os
import time
import pandas as pd
import pymysql
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    "host":   os.getenv("DB_HOST", "localhost"),
    "port":   int(os.getenv("DB_PORT", 3306)),
    "db":     os.getenv("DB_NAME", "hehe_db"),
    "user":   os.getenv("DB_USER", "root"),
    "passwd": os.getenv("DB_PASSWORD", ""),
    "charset": "utf8mb4",
}

INSERT_SQL = """
INSERT INTO tb_hospital (hospital_id, name, address, location, contact_number)
VALUES (%s, %s, %s, ST_GeomFromText(%s, 4326), %s)
"""

# tb_hospital에 ykiho 컬럼이 없으므로 name+address 조합으로 중복 체크
CHECK_SQL = "SELECT COUNT(*) FROM tb_hospital WHERE name = %s AND address = %s"


def make_wkt(lat: float, lng: float) -> str:
    """위도/경도 → WKT POINT 문자열 (SRID 4326: POINT(위도 경도))"""
    return f"POINT({lat} {lng})"


def main():
    script_dir = os.path.dirname(__file__)
    input_path = os.path.join(script_dir, "hospitals_with_coords.csv")

    if not os.path.exists(input_path):
        print(f"파일 없음: {input_path}\ngeocde_hospitals.py 를 먼저 실행하세요.")
        return

    df = pd.read_csv(input_path, dtype=str)
    df["XPos"] = pd.to_numeric(df["XPos"], errors="coerce")
    df["YPos"] = pd.to_numeric(df["YPos"], errors="coerce")
    df["coord_ok"] = pd.to_numeric(df.get("coord_ok", 1), errors="coerce").fillna(0)

    # 좌표 확보된 행만 처리
    df = df[df["coord_ok"] == 1].reset_index(drop=True)
    print(f"INSERT 대상: {len(df)}건")

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
            lat     = row["YPos"]   # YPos = 위도
            lng     = row["XPos"]   # XPos = 경도
            tel     = str(row.get("telno", "")).strip() or "정보없음"

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
                cursor.execute(INSERT_SQL, (hospital_id, name, address, wkt, tel))
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

    # INSERT 실패 병원 별도 파일로 저장
    if error_rows:
        fail_path = os.path.join(script_dir, "import_failed.csv")
        pd.DataFrame(error_rows).to_csv(fail_path, index=False, encoding="utf-8-sig")
        print(f"INSERT 실패 목록 저장: {fail_path} ({len(error_rows)}건)")


if __name__ == "__main__":
    main()