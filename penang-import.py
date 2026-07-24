import mysql.connector

data = """
PNG-NAI001	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0564SZ	PF-51DZ2V	SCM	SCM TXT	1F Office	2024-11-19	SCM TXT	1
PNG-NAI002	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0565SZ	PF-4NX6EH	Engineering	Sample.PNG	NPI Room	2024-11-28	工程	1
PNG-NAI003	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0566SZ	PF-50K9GH	Manufacturing	Trainer	2F Production workshop	2024-11-30	培训员	1
PNG-NAI004	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0567SZ	PF-50KJQY	SCM	SeePeng Tan	1F Office	2024-12-03		1
PNG-NAI005	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-553ZCH	Manufacturing	Mohd Norshisyam		2024-12-06		1
PNG-NAI006	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-5559M8	Manufacturing	Noor Emelia		2024-12-12		1
PNG-NAI007	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-52Q4RR			2024-12-17	财务保管-闲置	0
PNG-NAI008	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-53MJDQ	Quality	Raihan Shafiqah Kamarul Zaman	2F Production workshop	2024-12-19		1
PNG-NAI009	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-555DML	Manufacturing	Muhdabdul Malek	2F Production workshop	2024-12-19		1
PNG-NAI010	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-553QNV	SCM	Warehouse.PNG		2024-12-19		1
PNG-NAI011	海康监控平台	海康			IT	IT机房			1
PNG-NAI012	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4V9B6F	SCM	王磊		2025-03-12	仓库	1
PNG-NAI013	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4TEEEN	Quality	CheeLIN LOO			1
PNG-NAI014	Thinkpad 笔记本	Thinkpad E14-i5 Gen5		PF-4RDHC2	Manufacturing	NAI.Penang		生产线	1
PNG-NAI015	JAV会议一体机	JAV会议一体机			Manufacturing			1
PNG-NAI016	友达一体机	友达一体机			Manufacturing			1
PNG-NAI017	友达一体机	友达一体机			Manufacturing			1
PNG-NAI018	友达一体机	友达一体机			Manufacturing			1
PNG-NAI019	友达一体机	友达一体机			Manufacturing			1
PNG-NAI020	友达一体机	友达一体机			Manufacturing			1
PNG-NAI021	友达一体机	友达一体机			Manufacturing			1
PNG-NAI022	友达一体机	友达一体机			Manufacturing			1
PNG-NAI023	友达一体机	友达一体机			Manufacturing			1
PNG-NAI024	友达一体机	友达一体机			Manufacturing			1
PNG-NAI025	友达一体机	友达一体机			Manufacturing			1
PNG-NAI026	友达一体机	友达一体机			Manufacturing			1
PNG-NAI027	友达一体机	友达一体机			Manufacturing			1
PNG-NAI028	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF-5DYQK2	Engineering	KinFei Liong	NPI Room	2025-03-27		1
PNG-NAI029	JAV会议一体机	Jav一体机			Manufacturing		前台欢迎词显示		1
PNG-NAI030	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2RYQ	Manufacturing	William	2F Office	2025-04-24	给OM	1
PNG-NAI031	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PSE	SCM	dasily	1F Office	2025-04-24		1
PNG-NAI032	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PT8	Engineering	NPI Room	NAI Room	2025-04-30		1
PNG-NAI033	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2RZ4	Finance	SuSong Chin	1F Office	2025-05-16	离职转交HR-闲置	0
PNG-NAI034	Thinkpad 笔记本	Thinkpad E14-i5 Gen6		PF5G2PSS	HR	SIewLi Chuah	1F Office	2025-06-10		1
PNG-NAI035	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HTK	Engineering	HanSheng	NPI Room	2025-07-17		1
PNG-NAI036	Lenove一体机	Neo 50a 24 Gen 5 Desktop (ThinkCentre)		MP2ZA0ZY	Manufacturing	测试一体机		2025-07-17		1
PNG-NAI037	Lenove一体机	Neo 50a 24 Gen 5 Desktop (ThinkCentre)		MP2ZA0ZZ	Manufacturing	测试一体机		2025-07-17		1
PNG-NAI038	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HSA	HR	GiGi.Hee	1F Office	2025-07-21		1
PNG-NAI039	Thinkpad 笔记本	Thinkpad E14-i5 Gen7	CE0581SZ	PF5M9VLJ	Sales	Nixon	1F Office	2025-07-31	苏州借槟城（nixon)	1
PNG-NAI050	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5R7HSW	Manufacturing	WeiSheng.Saw		2025-08-08		1
PNG-NAI051	JAV会议一体机	JAV一体机			Manufacturing		2025-08-11		1
PNG-NAI052	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5L7T6J	Supply Chain	SzeYun Tan	1F Office	2025-08-13	离职转交HR暂时保留一个月	1
PNG-NAI053	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5L76MQ	Manufacturing	TihHon Beh		2025-08-27		1
CE0567SZ	Thinkpad 笔记本	Thinkpad E14-i5 Gen5	CE0497SZ	PF3YJM1Y	Manufacturing	NAI.Penang			1
PNG-NAI054	Thinkpad 笔记本	Thinkpad E14-i5 Gen7		PF5KWNAT	Finance	SinJie Lee (Vinnie Chen)	1F Office	2025-09-09		1
PNG-NAI055	ThinkCentre	NEO 50S G5 i5 14400			Manufacturing	Label Printer		2025-09-15	标签打印电脑	1
PNG-NAI056	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWH8	SCM	Warehouse	1F WareHouse	2025-09-15		1
PNG-NAI057	ThinkCentre	NEO 50S G5 i5 14400			SCM	Warehouse	1F WareHouse	2025-09-16		1
PNG-NAI058	AOC一体机	AOC一体机			Manufacturing		2025-10-13		1
PNG-NAI059	生产测试机	生产测试机			Manufacturing		2025-11-06		1
PNG-NAI060	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWHE	Engineering	Sample.png	NPI Room	2025-12-01		1
PNG-NAI061	ThinkCentre	NEO 50S G5 i5 14400		YLT1CWHF	Manufacturing	袋标打印	2F Production workshop			1
PNG-NAI062	Thinkpad 笔记本	Thinkpad E14		PF62QXGX	HR	HR实习生			1
PNG-NAI077	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62HYJN	Manufacturing			1
PNG-NAI078	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62QXEM	Manufacturing			1
PNG-NAI079	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62RNXM	Manufacturing			1
PNG-NAI080	Thinkpad 笔记本	Thinkpad E14 21SX0002MA		PF62T5ZY	Manufacturing	阿苏		阿苏	0
""".strip()

conn = mysql.connector.connect(
    host='localhost', user='root', password='CHNX#000',
    database='it_asset_manage', charset='utf8mb4')
cur = conn.cursor()

# Ensure departments exist
depts = set()
for line in data.split('\n'):
    parts = line.split('\t')
    if len(parts) > 5 and parts[5].strip():
        depts.add(parts[5].strip())
for d in depts:
    cur.execute("INSERT IGNORE INTO sys_department (site, dept_name, status) VALUES ('Penang', %s, 1)", (d,))

# Ensure categories exist
cats = set()
for line in data.split('\n'):
    parts = line.split('\t')
    if len(parts) > 1 and parts[1].strip():
        cats.add(parts[1].strip())
for c in cats:
    cur.execute("INSERT IGNORE INTO asset_category (category_name, sort_order) VALUES (%s, 99)", (c,))

# Insert assets
count = 0
for line in data.split('\n'):
    parts = line.split('\t')
    if not parts[0].strip(): continue
    name = parts[0].strip()
    cat = parts[1].strip() if len(parts) > 1 else ''
    model = parts[2].strip() if len(parts) > 2 else ''
    code = parts[3].strip() if len(parts) > 3 else ''
    sn = parts[4].strip() if len(parts) > 4 else ''
    dept = parts[5].strip() if len(parts) > 5 else ''
    user = parts[6].strip() if len(parts) > 6 else ''
    loc = parts[7].strip() if len(parts) > 7 else ''
    dt = parts[8].strip() if len(parts) > 8 else None
    remark = parts[9].strip() if len(parts) > 9 else ''
    status = int(parts[10]) if len(parts) > 10 and parts[10].strip() else 1
    
    cur.execute("""INSERT INTO asset_info 
        (site, asset_code, asset_name, category_id, model, serial_number, department, user_name, 
         storage_location, purchase_date, remark, status, create_time)
        VALUES ('Penang', %s, %s, 
          (SELECT MIN(category_id) FROM asset_category WHERE category_name=%s),
          %s, %s, %s, %s, %s, 
          NULLIF(%s,''), %s, %s, NOW())""",
        (code, name, cat, model, sn, dept, user, loc, dt, remark, status))
    count += 1

conn.commit()
print(f"Inserted {count} assets into Penang site")
cur.execute("SELECT site, COUNT(*) FROM asset_info GROUP BY site")
for r in cur.fetchall(): print(r)
conn.close()
