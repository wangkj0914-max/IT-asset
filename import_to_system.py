import subprocess, json, time, os

BASE_URL = "http://localhost:8080/asset"

# 1. 登录获取 token
print("正在登录...")
login_cmd = [
    "curl", "-s", "-X", "POST", f"{BASE_URL}/login",
    "-H", "Content-Type: application/json",
    "-d", json.dumps({"username": "admin", "password": "123456"})
]
result = subprocess.run(login_cmd, capture_output=True, text=True)
login_data = json.loads(result.stdout)
if login_data.get("code") != 200:
    print(f"登录失败: {login_data}")
    exit(1)

token = login_data["data"]["token"]
print(f"登录成功，token获取完成")

# 2. 读取 Excel 数据
import pandas as pd
df = pd.read_excel(r"E:\固定资产系统\资产导入_电脑盘点数据.xlsx", sheet_name="导入模板")
print(f"读取 {len(df)} 条数据，开始导入...")

success = 0
fail = 0
errors = []

for idx, row in df.iterrows():
    asset_name = str(row.get("资产名称(必填)", "")).strip()
    if not asset_name or asset_name == "nan":
        fail += 1
        errors.append(f"第{idx+2}行: 资产名称为空")
        continue

    category_id = row.get("分类ID(必填)")
    if pd.isna(category_id):
        category_id = 2

    sn = str(row.get("序列号(必填)", "")).strip()
    if sn == "nan" or not sn:
        sn = f"AUTO-{idx+1}"

    # 构造 JSON
    data = {
        "assetName": asset_name,
        "categoryId": int(category_id),
        "status": int(row.get("资产状态(0-3)", 0)) if pd.notna(row.get("资产状态(0-3)")) else 0,
    }
    brand = str(row.get("品牌", "")).strip()
    if brand and brand != "nan":
        data["brand"] = brand
    model = str(row.get("型号", "")).strip()
    if model and model != "nan":
        data["model"] = model
    data["serialNumber"] = sn
    loc = str(row.get("存放位置(必填)", "未知")).strip()
    if loc and loc != "nan":
        data["storageLocation"] = loc
    remark = str(row.get("备注", "")).strip()
    if remark and remark != "nan":
        data["remark"] = remark
    price = row.get("采购价格")
    if pd.notna(price):
        data["purchasePrice"] = float(price)
    pdate = row.get("采购日期(YYYY-MM-DD)")
    if pd.notna(pdate):
        data["purchaseDate"] = str(pdate)[:10]

    # 调用 save 接口
    curl_cmd = [
        "curl", "-s", "-X", "POST", f"{BASE_URL}/assetInfo/save",
        "-H", f"Authorization: {token}",
        "-H", "Content-Type: application/json",
        "-d", json.dumps(data, ensure_ascii=False)
    ]
    r = subprocess.run(curl_cmd, capture_output=True, text=True)
    try:
        resp = json.loads(r.stdout)
        if resp.get("code") == 200:
            success += 1
            if success % 50 == 0:
                print(f"  进度: {success}/{len(df)}")
        else:
            fail += 1
            msg = resp.get("msg", r.stdout[:100])
            errors.append(f"第{idx+2}行 [{asset_name}]: {msg}")
    except Exception as e:
        fail += 1
        errors.append(f"第{idx+2}行 [{asset_name}]: {r.stdout[:100]}")

    time.sleep(0.03)

print(f"\n导入完成！")
print(f"成功: {success} 条")
print(f"失败: {fail} 条")
if errors:
    print("\n失败详情（前10条）：")
    for e in errors[:10]:
        print(f"  {e}")
    if len(errors) > 10:
        print(f"  ... 共 {len(errors)} 条失败")
