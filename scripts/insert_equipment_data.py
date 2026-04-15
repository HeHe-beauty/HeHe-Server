"""
젠틀맥스프로(equip_id=1) 기기 데이터 처리 스크립트
1. tb_hospital.contact_url / contact_number 가 'nan' 인 경우 업데이트
2. 리뉴미피부과의원 화곡 — tb_hospital 신규 INSERT
3. tb_hospital_equipment INSERT (equip_id=1, total_count=1)

실행: python insert_equipment_data.py
"""

import os
import time
import pymysql
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    "host":    os.getenv("DB_HOST", "localhost"),
    "port":    int(os.getenv("DB_PORT", 3306)),
    "db":      os.getenv("DB_NAME", "hehe_db"),
    "user":    os.getenv("DB_USER", "root"),
    "passwd":  os.getenv("DB_PASSWORD", ""),
    "charset": "utf8mb4",
}

EQUIP_ID = 1  # 젠틀맥스프로

# 젠틀맥스프로 보유 병원 데이터
# (hospital_id, contact_number, contact_url)
# hospital_id=None : tb_hospital 신규 INSERT 필요
HOSPITAL_DATA = [
    (1776089861974, "02-325-9990",    "https://www.9skin2.co.kr/"),
    (1776089861975, "02-599-9990",    "https://www.9skin1.co.kr/"),
    (1776089861993, "02-3141-8864",   None),
    (1776089861824, "02-6952-7131",   "https://evers25.co.kr/"),
    (1776089864339, "02-2039-3675",   "https://www.evers28.co.kr/"),
    (1776089862187, "02-749-7738",    None),
    (1776089862219, "02-2138-2225",   "https://daybeauclinic07.com/branch/"),
    (1776089862221, "02-3665-0399",   "https://www.daybeauclinic01.com/branch/"),
    (1776089862218, "02-465-7791",    "https://daybeauclinic04.com/branch/"),
    (1776089862220, "02-2135-5664",   "https://www.daybeauclinic08.com/branch/"),
    (1776089862225, "050-7873-0460",  "https://www.delphic.co.kr/"),
    (1776089862251, "0507-1300-1524", "https://dc.cellodyskin.com/"),
    (1776089862253, "0507-1367-7226", "https://www.dewydclinic.com/"),
    (1776089861757, "02-6010-1300",   "https://www.global.the-director.co.kr/ko"),
    (1776089863161, "02-2138-3389",   "https://dion-sl.com/"),
    (1776089862360, "02-6951-2522",   "http://www.rest-clinic.com/"),
    (1776089862365, "02-517-5300",    "http://www.rachelclinic.com/"),
    (1776089862421, "02-2275-5085",   "https://www.rnmeskin.com/medical/medical_0603.htm?numb=3"),
    (1776089862422, "02-588-2304",    "https://www.rnmeskin.com/medical/medical_06.htm?numb=5"),
    (1776089862424, "02-413-2303",    "https://www.rnmeskin.com/medical/medical_0605.htm?numb=1"),
    (1776089862423, "02-394-8275",    "https://www.rnmeskin.com/medical/medical_0602.htm?numb=2"),
    (1776089862447, "1544-0890",      "https://www.reverseclinic.com/"),
    (1776089862444, "1544-0890",      "https://hd.reverseclinic.com/"),
    (1776089862466, "02-785-0095",    "http://www.rennew.co.kr"),
    (1776089862483, "02-543-0210",    "http://www.reoneskin.com/"),
    (1776089862496, "02-561-4555",    "https://www.drritz.co.kr/"),
    (1776089862540, "02-595-9567",    "http://labellaclinic.com/"),
    (1776089862605, "02-3442-3330",   "http://www.meticlinic.com"),
    (1776089862631, "02-6232-5732",   "https://modern-standard.co.kr/"),
    (1776089862638, "02-555-6231",    "https://molessclinic.com/"),
    (1776089862683, "02-2138-2805",   "https://www.mutoelps.com/"),
    (1776089862754, "02-6367-1212",   "https://dosan.miliclinic.com"),
    (1776089862755, "02-6337-1212",   "https://sinchon.miliclinic.com"),
    (1776089861767, "02-6956-0133",   "https://gangnam.vandsclinic.co.kr/"),
    (1776089862816, "02-6956-0097",   "https://guro.vandsclinic.co.kr/"),
    (1776089862240, "02-6956-0876",   "https://dongdaemun.vandsclinic.co.kr/"),
    (1776089862809, "02-6956-5516",   "https://myeongdong.vandsclinic.co.kr/"),
    (1776089862613, "02-6956-5113",   "https://myeongdong2.vandsclinic.co.kr/"),
    (1776089863012, "02-6956-0035",   "https://samseong.vandsclinic.co.kr/"),
    (1776089862812, "02-6956-0046",   "https://seongsu.vandsclinic.co.kr/"),
    (1776089862815, "02-6956-0375",   "http://webzine.smiletoc.co.kr/webzine/vandsclinic/202507_si_open/"),
    (1776089862811, "02-6956-0508",   "https://yeouido.vandsclinic.co.kr/"),
    (1776089863743, "02-6956-0780",   "https://yeongdeungpo.vandsclinic.co.kr/"),
    (1776089862814, "02-6956-0059",   "https://yongsan.vandsclinic.co.kr"),
    (1776089862813, "02-6956-0212",   "https://cheonho.vandsclinic.co.kr/"),
    (1776089864272, "02-6956-0606",   "https://cheongdam.vandsclinic.co.kr/"),
    (1776089862810, "02-6956-3230",   "https://hwagok.vandsclinic.co.kr/"),
    (1776089862838, "02-518-7117",    "https://bailorclinic.com/"),
    (1776089862892, "02-476-5955",    "https://www.beautyskyclinic.com/"),
    (1776089862893, "02-3144-0107",   "https://beautystone1.com/"),
    (1776089862897, "02-2668-7785",   "https://gangseo.beautyon.co.kr/"),
    (1776089862896, "02-6958-8299",   "https://dolgoji.beautyon.co.kr/"),
    (1776089862978, "0507-1306-8438", "https://pposong.clinic/"),
    (1776089863050, "02-1544-5236",   "https://gangseo.shinebeam.co.kr/"),
    (1776089863058, "02-376-9962",    "https://www.wwskin.com/"),
    (1776089863180, "02-6205-3430",   "https://www.gangnam.ceramiqueclinic.com"),
    (1776089863190, "02-2088-5928",   "https://www.instagram.com/seye_clinic/"),
    (1776089863213, "02-532-2550",    "https://www.cellinclinic9.com/"),
    (1776089863216, "02-3789-0824",   "https://www.cellinclinic8.com/"),
    (1776089863211, "02-355-7474",    "http://www.cellinclinic.com/"),
    (1776089863214, "02-6949-3663",   "https://www.cellinclinic13.com/"),
    (1776089863215, "1800-6156",      "https://www.cellinclinic4.com/"),
    (1776089863250, "02-6951-2159",   "https://www.sunmo.co.kr/"),
    (1776089863270, "02-558-2700",    "http://www.snowprs.com/"),
    (1776089863271, "02-2038-9228",   "https://nowon-snow.co.kr/"),
    (1776089863272, "02-2138-0770",   "https://magok-snow.co.kr/"),
    (1776089863283, "02-546-7582",    "https://skinda.co.kr/"),
    (1776089863293, "0507-1435-1670", "http://www.spadeclinic.com/"),
    (1776089863295, "0507-1333-2357", "https://sloaclinic.com/"),
    (1776089863302, "02-512-7117",    "https://gangnam.sia-clinic.com/"),
    (1776089863343, "02-739-0119",    "https://www.summitclinic.co.kr/"),
    (1776089863349, "02-987-9981",    "http://www.agafarclinic.com/clinic/mia/index.html"),
    (1776089863356, "02-6952-4880",   "https://alohaskin.co.kr/"),
    (1776089863372, "02-425-2411",    "https://www.episode.clinic/"),
    (1776089863390, "1544-0377",      "https://abijouclinicnw.com/"),
    (1776089863456, "02-517-9337",    "https://eternaclinic.co.kr/"),
    (1776089863579, "02-543-3110",    "https://www.amred.co.kr/"),
    (1776089863791, "02-2659-2888",   "https://oganacell-magok.com/"),
    (1776089863792, "02-6956-3049",   "https://www.ogayeclinic.com/"),
    (1776089863832, "02-2016-5000",   "https://samseong.own-clinic.com/"),
    (1776089863591, "02-6951-5582",   "https://www.yuduni114.co.kr/"),
    (1776089863955, "02-2636-6020",   "https://www.ydpuni114.co.kr/"),
    (1776089864328, "02-3443-1777",   "https://www.instagram.com/chungdamqbq"),
    (1776089864401, "02-6958-8906",   "https://classoneclinic.com/"),
    (1776089864406, "0507-1331-1755", "https://hongdae.cleorclinic.com/ko"),
    (1776089864447, "02-537-4842",    "https://www.toxnfill1.com/index.php"),
    (1776089864690, "0507-1364-4842", "https://www.toxnfill50.com/"),
    (1776089864454, "02-6941-1966",   "https://www.tonesclinic.co.kr/geondae"),
    (1776089864463, "02-6237-7771",   "http://www.triomphedr.com/"),
    (1776089864476, "02-2296-7007",   "http://pastelskin.com/"),
    (1776089864490, "0507-1313-8176", "https://www.pecheskin.clinic/ko"),
    (1776089864493, "1661-5619",      "https://cheonho.facefilter.kr/"),
    (1776089864498, "02-325-7979",    "https://www.forenaclinic.com/"),
    (1776089864524, "02-332-2648",    "http://fullface.co.kr/"),
    (1776089864532, "02-2038-8910",   "https://www.projectuclinic.com/"),
    (1776089864584, "02-6858-7897",   "https://www.adelclinic.com/"),
    (1776089864639, "02-775-6100",    "http://happymedi.net"),
    (1776089864675, "02-2214-0022",   "http://hsplus-skin.co.kr/index.asp"),
    (1776089864709, "02-1600-5940",   "http://www.ngshuman.co.kr/"),
    (1776089864712, "02-792-8555",    "http://yshuman.co.kr"),
]

# 신규 INSERT 병원: 리뉴미피부과의원 화곡
NEW_HOSPITAL = {
    "name":           "리뉴미피부과의원",
    "address":        "서울특별시 강서구 화곡3동 1065-9 3층 (로이빌딩 3층)",
    "lat":            37.5415318025648,
    "lng":            126.83901802056,
    "contact_number": "02-2607-7574",
    "contact_url":    "https://www.rnmeskin.com/medical/medical_0604.htm?numb=4",
}


def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor(pymysql.cursors.DictCursor)

    # ── 1. 리뉴미피부과의원 화곡 INSERT ──────────────────────────────────
    print("=== 1. 리뉴미피부과의원 화곡 INSERT ===")
    new_hospital_id = int(time.time() * 1000)
    wkt = f"POINT({NEW_HOSPITAL['lat']} {NEW_HOSPITAL['lng']})"
    cursor.execute(
        """
        INSERT INTO tb_hospital (hospital_id, name, address, location, contact_number, contact_url)
        VALUES (%s, %s, %s, ST_GeomFromText(%s, 4326), %s, %s)
        """,
        (new_hospital_id, NEW_HOSPITAL["name"], NEW_HOSPITAL["address"],
         wkt, NEW_HOSPITAL["contact_number"], NEW_HOSPITAL["contact_url"])
    )
    conn.commit()
    print(f"  INSERT 완료 → hospital_id: {new_hospital_id}")

    # ── 2. contact_url / contact_number 업데이트 ─────────────────────────
    print("\n=== 2. contact_url / contact_number 업데이트 ===")
    url_updated = 0
    tel_updated = 0
    not_found = 0

    for hospital_id, tel, url in HOSPITAL_DATA:
        cursor.execute(
            "SELECT hospital_id, contact_number, contact_url FROM tb_hospital WHERE hospital_id = %s",
            (hospital_id,)
        )
        row = cursor.fetchone()
        if not row:
            print(f"  [미발견] hospital_id={hospital_id}")
            not_found += 1
            continue

        # contact_url이 'nan'이고 플랜에 url이 있으면 업데이트
        if url and row["contact_url"] in ("nan", None, ""):
            cursor.execute(
                "UPDATE tb_hospital SET contact_url = %s WHERE hospital_id = %s",
                (url, hospital_id)
            )
            url_updated += 1

        # contact_number가 'nan' 또는 '정보없음'이면 업데이트
        if tel and row["contact_number"] in ("nan", "정보없음", None, ""):
            cursor.execute(
                "UPDATE tb_hospital SET contact_number = %s WHERE hospital_id = %s",
                (tel, hospital_id)
            )
            tel_updated += 1

    conn.commit()
    print(f"  contact_url 업데이트: {url_updated}건")
    print(f"  contact_number 업데이트: {tel_updated}건")
    print(f"  미발견(hospital_id 불일치): {not_found}건")

    # ── 3. tb_hospital_equipment INSERT ──────────────────────────────────
    print("\n=== 3. tb_hospital_equipment INSERT ===")

    # 기존 데이터의 hospital_id 목록
    existing_ids = [row[0] for row in HOSPITAL_DATA]
    # 신규 INSERT한 화곡점 추가
    all_ids = existing_ids + [new_hospital_id]

    equip_inserted = 0
    equip_skipped = 0

    for hospital_id in all_ids:
        # hospital_id가 tb_hospital에 실제로 존재하는지 확인
        cursor.execute("SELECT 1 FROM tb_hospital WHERE hospital_id = %s", (hospital_id,))
        if not cursor.fetchone():
            equip_skipped += 1
            continue

        # 이미 존재하면 스킵
        cursor.execute(
            "SELECT 1 FROM tb_hospital_equipment WHERE hospital_id = %s AND equip_id = %s",
            (hospital_id, EQUIP_ID)
        )
        if cursor.fetchone():
            equip_skipped += 1
            continue

        cursor.execute(
            "INSERT INTO tb_hospital_equipment (hospital_id, equip_id, total_count) VALUES (%s, %s, 1)",
            (hospital_id, EQUIP_ID)
        )
        equip_inserted += 1

    conn.commit()
    print(f"  tb_hospital_equipment INSERT: {equip_inserted}건")
    print(f"  스킵(미존재 또는 중복): {equip_skipped}건")

    # ── 4. 결과 요약 ──────────────────────────────────────────────────────
    cursor.execute("SELECT COUNT(*) AS cnt FROM tb_hospital_equipment WHERE equip_id = %s", (EQUIP_ID,))
    total = cursor.fetchone()["cnt"]
    print(f"\n완료. tb_hospital_equipment equip_id={EQUIP_ID} 총 {total}건")

    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()