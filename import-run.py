import mysql.connector

data = open("E:/it-asset-system/penang-import.py", "r", encoding="utf-8").read()
start = data.find("data = \"\"\"")
end = data.find("\"\"\".strip()", start)
lines = data[start+9:end].strip().split('\n')

conn = mysql.connector.connect(
    host='localhost', user='root', password='CHNX#000',
    database='it_asset_manage', charset='utf8mb4')
cur = conn.cursor()

# Ensure departments
depts = set()
for l in lines:
    p = l.strip().split('\t')
    if len(p) > 5 and p[5].strip(): depts.add(p[5].strip())
for d in depts:
    cur.execute("INSERT IGNORE INTO sys_department (site, dept_name, status) VALUES ('Penang', %s, 1)", (d,))

# Ensure categories
cats = set()
for l in lines:
    p = l.strip().split('\t')
    if len(p) > 1 and p[1].strip(): cats.add(p[1].strip())
for c in cats:
    cur.execute("INSERT IGNORE INTO asset_category (category_name, sort_order) VALUES (%s, 99)", (c,))

count = errors = 0
for l in lines:
    l = l.strip()
    if not l or l.startswith('#'): continue
    p = l.split('\t')
    while len(p) < 11: p.append('')
    
    name, cat, model, code, sn = p[0].strip(), p[1].strip(), p[2].strip(), p[3].strip(), p[4].strip()
    dept, user, loc, dt_s, remark = p[5].strip(), p[6].strip(), p[7].strip(), p[8].strip(), p[9].strip()
    try: status = int(p[10].strip()) if p[10].strip() else 1
    except: status = 1
    
    dt = dt_s if dt_s else None
    try:
        cur.execute("""INSERT INTO asset_info 
            (site, asset_code, asset_name, category_id, model, serial_number, department, user_name, 
             storage_location, purchase_date, remark, status, create_time)
            VALUES ('Penang', %s, %s, 
              (SELECT MIN(category_id) FROM asset_category WHERE category_name=%s),
              %s, %s, %s, %s, %s, %s, %s, %s, NOW())""",
            (code, name, cat, model, sn, dept, user, loc, dt, remark, status))
        count += 1
    except Exception as e:
        errors += 1
        print(f"  SKIP {name}: {e}")

conn.commit()
print(f"Inserted {count} assets, {errors} errors")
cur.execute("SELECT site, COUNT(*) FROM asset_info GROUP BY site")
for r in cur.fetchall(): print(f"  {r[0]}: {r[1]}")
conn.close()
