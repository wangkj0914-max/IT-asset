import mysql.connector, re

raw = [
"PNG-NAI001	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0564SZ	PF-51DZ2V	SCM	SCM TXT	1F Office	2024-11-19	SCM TXT	在用",
"PNG-NAI002	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0565SZ	PF-4NX6EH	Engineering	Sample.PNG	NPI Room	2024-11-28	工程	在用",
"PNG-NAI003	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0566SZ	PF-50K9GH	Manufacturing	Trainer	2F Production workshop	2024-11-30	培训员	在用",
"PNG-NAI004	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0567SZ	PF-50KJQY	SCM	SeePeng Tan	1F Office	2024-12-03		在用",
"PNG-NAI005	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-553ZCH	Manufacturing	Mohd Norshisyam		2024-12-06		在用",
"PNG-NAI006	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-5559M8	Manufacturing	Noor Emelia		2024-12-12		在用",
"PNG-NAI007	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-52Q4RR			2024-12-17	财务保管-闲置	闲置",
"PNG-NAI008	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-53MJDQ	Quality	Raihan Shafiqah Kamarul Zaman	2F Production workshop	2024-12-19		在用",
"PNG-NAI009	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-555DML	Manufacturing	Muhdabdul Malek	2F Production workshop	2024-12-19		在用",
"PNG-NAI010	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-553QNV	SCM	Warehouse.PNG		2024-12-19		在用",
"PNG-NAI011	海康监控平台	海康			IT	IT机房			在用",
"PNG-NAI012	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4V9B6F	SCM	王磊		2025-03-12	仓库	在用",
"PNG-NAI013	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4TEEEN	Quality	CheeLIN LOO			在用",
"PNG-NAI014	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4RDHC2	Manufacturing	NAI.Penang		生产线	在用",
"PNG-NAI015	JAV会议一体机	JAV会议一体机			Manufacturing			在用",
"PNG-NAI028	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF-5DYQK2	Engineering	KinFei Liong	NPI Room	2025-03-27		在用",
"PNG-NAI029	JAV会议一体机	Jav一体机			Manufacturing		前台欢迎词显示	在用",
"PNG-NAI030	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2RYQ	Manufacturing	William	2F Office	2025-04-24	给OM	在用",
"PNG-NAI031	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PSE	SCM	dasily	1F Office	2025-04-24		在用",
"PNG-NAI032	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PT8	Engineering	NPI Room	NAI Room	2025-04-30		在用",
"PNG-NAI033	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2RZ4	Finance	SuSong Chin	1F Office	2025-05-16	离职转交HR-闲置	闲置",
"PNG-NAI034	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PSS	HR	SIewLi Chuah	1F Office	2025-06-10		在用",
"PNG-NAI035	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HTK	Engineering	HanSheng	NPI Room	2025-07-17		在用",
"PNG-NAI036	Lenove一体机	Neo 50a 24 Gen 5 Desktop (ThinkCentre)		MP2ZA0ZY	Manufacturing	测试一体机		2025-07-17		在用",
"PNG-NAI037	Lenove一体机	Neo 50a 24 Gen 5 Desktop (ThinkCentre)		MP2ZA0ZZ	Manufacturing	测试一体机		2025-07-17		在用",
"PNG-NAI038	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HSA	HR	GiGi.Hee	1F Office	2025-07-21		在用",
"PNG-NAI039	Thinkpad 笔记本	Thinkpad E14-i5 Gen7	CE0581SZ	PF5M9VLJ	Sales	Nixon	1F Office	2025-07-31	苏州借槟城（nixon)	在用",
"PNG-NAI050	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HSW	Manufacturing	WeiSheng.Saw		2025-08-08		在用",
"PNG-NAI051	JAV会议一体机	JAV一体机			Manufacturing		2025-08-11		在用",
"PNG-NAI052	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5L7T6J	Supply Chain	SzeYun Tan	1F Office	2025-08-13	离职转交HR暂时保留一个月	在用",
"PNG-NAI053	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5L76MQ	Manufacturing	TihHon Beh		2025-08-27		在用",
"CE0567SZ	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0497SZ	PF3YJM1Y	Manufacturing	NAI.Penang			在用",
"PNG-NAI054	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5KWNAT	Finance	SinJie Lee (Vinnie Chen)	1F Office	2025-09-09		在用",
"PNG-NAI055	ThinkCentre	NEO 50S G5 i5 14400			Manufacturing	Label Printer		2025-09-15	标签打印电脑	在用",
"PNG-NAI056	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWH8	SCM	Warehouse	1F WareHouse	2025-09-15		在用",
"PNG-NAI057	ThinkCentre	NEO 50S G5 i5 14400			SCM	Warehouse	1F WareHouse	2025-09-16		在用",
"PNG-NAI058	AOC一体机	AOC一体机			Manufacturing		2025-10-13		在用",
"PNG-NAI059	生产测试机	生产测试机			Manufacturing		2025-11-06		在用",
"PNG-NAI060	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWHE	Engineering	Sample.png	NPI Room	2025-12-01		在用",
"PNG-NAI061	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWHF	Manufacturing	袋标打印	2F Production workshop			在用",
"PNG-NAI062	Thinkpad 笔记本	Thinkpad E14		PF62QXGX	HR	HR实习生			在用",
"PNG-NAI077	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62HYJN	Manufacturing			在用",
"PNG-NAI078	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62QXEM	Manufacturing			在用",
"PNG-NAI079	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62RNXM	Manufacturing			在用",
"PNG-NAI080	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62T5ZY	Manufacturing	阿苏		阿苏	闲置",
"CE0669SZ	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0563SZ			2024-07-30		在用",
"CE0363SZ	HP笔记本	HP ProBook 440 G4	CE0390SZ				在用",
"CE0398SZ	DELL一体机	测试电脑	CE0419SZ				在用",
"CE0399SZ	DELL一体机	测试电脑			2016-06-01		在用",
"CE0548SZ	DELL一体机	测试电脑				在用",
"CE0678SZ	HP笔记本	HP ProBook 440 G3				在用",
"CE0343SZ	HP笔记本	HP 440G3 Laptop	CE0360SZ				在用",
]

conn = mysql.connector.connect(
    host='localhost', user='root', password='CHNX#000',
    database='it_asset_manage', charset='utf8mb4')
cur = conn.cursor()

DATE_PAT = re.compile(r'^\d{4}[-/]\d{1,2}[-/]\d{1,2}$')

# Departments & categories
depts, cats = set(), set()
for l in raw:
    p = l.split('\t')
    if len(p) > 5 and p[5].strip(): depts.add(p[5].strip())
    if len(p) > 1 and p[1].strip(): cats.add(p[1].strip())
for d in depts: cur.execute("INSERT IGNORE INTO sys_department (site, dept_name, status) VALUES ('Penang', %s, 1)", (d,))
for c in cats: cur.execute("INSERT IGNORE INTO asset_category (category_name, sort_order) VALUES (%s, 99)", (c,))

count = 0
for l in raw:
    p = l.split('\t')
    while len(p) < 12: p.append('')
    
    name = p[0].strip()
    cat = p[1].strip()
    model = p[2].strip()
    code = p[3].strip()
    sn = p[4].strip()
    dept = p[5].strip()
    
    # Smart parse: check if field 6 looks like a date (skipping location)
    # If p[6] is not a date, it's user_name, p[7] is location or date
    # Normal: p[6]=user, p[7]=location, p[8]=date, p[9]=remark, p[10]=status
    # Missing date: p[6]=user, p[7]=remark(or empty), p[8]=status(or remark), p[9]=status
    has_7 = bool(p[7].strip())
    has_8 = bool(p[8].strip())
    
    user = p[6].strip()
    loc = p[7].strip()
    dt_raw = p[8].strip()
    remark = p[9].strip()
    stat_text = p[10].strip()
    
    # If p[8] doesn't look like a date, fields are shifted
    if dt_raw and not DATE_PAT.match(dt_raw) and not DATE_PAT.match(p[7].strip()):
        # No date at all - shift right
        remark = p[7].strip()
        stat_text = p[8].strip() or p[9].strip()
        loc = ''
        dt_raw = ''
    elif not DATE_PAT.match(dt_raw) and len(p) > 9:
        # Check if p[7] is date and p[8] is remark
        if DATE_PAT.match(p[7].strip()):
            dt_raw = p[7].strip()
            remark = p[8].strip()
            stat_text = p[9].strip()
            loc = ''
    
    status = 1 if '闲置' not in stat_text else 0
    
    try:
        cur.execute("""INSERT INTO asset_info 
            (site, asset_code, asset_name, category_id, model, serial_number, department, user_name, 
             storage_location, purchase_date, remark, status, create_time)
            VALUES ('Penang', %s, %s, 
              (SELECT MIN(category_id) FROM asset_category WHERE category_name=%s),
              %s, %s, %s, %s, %s, NULLIF(%s,''), %s, %s, NOW())""",
            (code, name, cat, model, sn, dept, user, loc, dt_raw, remark, status))
        count += 1
    except Exception as e:
        print(f"  SKIP {name}: {e}")

conn.commit()
print(f"Inserted {count} assets")
cur.execute("SELECT site, COUNT(*) FROM asset_info GROUP BY site")
for r in cur.fetchall(): print(f"  {r[0]}: {r[1]}")
conn.close()
