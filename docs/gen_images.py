from PIL import Image, ImageDraw, ImageFont
import os

W, H = 960, 540  # 16:9 at reasonable resolution
out_dir = "E:/it-asset-system/docs"

# ===== IMAGE 1: BEFORE - Excel management (red/warning themed) =====
img_before = Image.new("RGB", (W, H), "#FFF5F5")
draw = ImageDraw.Draw(img_before)

# Title bar
draw.rectangle([0, 0, W, 50], fill="#DC2626")
draw.text((20, 10), "BEFORE: Excel 管理模式", fill="white")

# Excel-like grid
for row in range(10):
    y = 60 + row * 42
    color = "#FEE2E2" if row == 0 else "#FFFFFF"
    draw.rectangle([10, y, W-10, y+42], fill=color, outline="#FCA5A5")
    if row == 0:
        draw.text((25, y+8), "资产��号", fill="#991B1B")
        draw.text((130, y+8), "资产名称", fill="#991B1B")
        draw.text((280, y+8), "部门", fill="#991B1B")
        draw.text((380, y+8), "状态", fill="#991B1B")
        draw.text((450, y+8), "折旧", fill="#991B1B")
        draw.text((550, y+8), "价值", fill="#991B1B")
        draw.text((650, y+8), "录入日期", fill="#991B1B")
        draw.text((770, y+8), "备注", fill="#991B1B")
    elif row == 1:
        draw.text((25, y+8), "CE0732SZ", fill="#7F1D1D")
        draw.text((130, y+8), "HP 288 G9", fill="#7F1D1D")
        draw.text((280, y+8), "Production", fill="#7F1D1D")
        draw.text((380, y+8), "在用", fill="#7F1D1D")
        draw.text((450, y+8), "??", fill="#DC2626")
        draw.text((550, y+8), "???", fill="#DC2626")
        draw.text((650, y+8), "2023/03", fill="#7F1D1D")
        draw.text((770, y+8), "谁在用?", fill="#DC2626")
    elif row == 2:
        draw.text((25, y+8), "CE0376SZ", fill="#7F1D1D")
        draw.text((130, y+8), "HP 480G2", fill="#7F1D1D")
        draw.text((280, y+8), "IT", fill="#7F1D1D")
        draw.text((380, y+8), "报废?", fill="#DC2626")
        draw.text((450, y+8), "-", fill="#7F1D1D")
        draw.text((550, y+8), "?", fill="#DC2626")
        draw.text((650, y+8), "2013/06", fill="#7F1D1D")
        draw.text((770, y+8), "", fill="#7F1D1D")
    else:
        draw.text((25, y+8), "...", fill="#9CA3AF")
        draw.text((130, y+8), "...", fill="#9CA3AF")

# Pain points
pain_y = 480
draw.rectangle([10, pain_y, W-10, H-10], fill="#FEF2F2", outline="#FCA5A5")
texts = [
    "X  手工录入，效率低易出错",
    "X  折旧靠人工算，不准确",
    "X  资产状态不实时，找不到责任人"
]
for i, t in enumerate(texts):
    draw.text((20, pain_y+5+i*18), t, fill="#991B1B")

img_before.save(f"{out_dir}/before_excel.png")
print("Before image saved")

# ===== IMAGE 2: AFTER - System (green/success themed) =====
img_after = Image.new("RGB", (W, H), "#F0FDF9")
draw = ImageDraw.Draw(img_after)

# Title bar
draw.rectangle([0, 0, W, 50], fill="#028090")
draw.text((20, 10), "AFTER: IT固定资产管理系统", fill="white")

# Dashboard cards
cards = [
    ("380+", "资产总数", "#028090"),
    ("34", "数据表", "#00A896"),
    ("82", "API接口", "#02C39A"),
    ("双站点", "苏州+Penang", "#0E7490"),
]
for i, (num, label, color) in enumerate(cards):
    cx = 20 + i * 235
    draw.rectangle([cx, 65, cx+215, 130], fill="white", outline="#D1FAE5")
    draw.rectangle([cx, 65, cx+215, 73], fill=color)
    draw.text((cx+10, 80), num, fill=color)
    draw.text((cx+10, 105), label, fill="#1A1A2E")

# System preview (simulated table view)
draw.text((20, 145), "固定资产管理界面", fill="#1A1A2E")
draw.rectangle([20, 170, W-20, 400], fill="white", outline="#A7F3D0")
for row in range(6):
    y = 175 + row * 35
    if row == 0:
        draw.rectangle([20, y, W-20, y+35], fill="#028090")
        draw.text((35, y+8), "资产编号", fill="white")
        draw.text((140, y+8), "资产名称", fill="white")
        draw.text((290, y+8), "分类", fill="white")
        draw.text((420, y+8), "状态", fill="white")
        draw.text((510, y+8), "当前价值", fill="white")
        draw.text((620, y+8), "EOL日期", fill="white")
        draw.text((760, y+8), "下次维护", fill="white")
    elif row == 1:
        draw.text((35, y+8), "ZC2026070288", fill="#1A1A2E")
        draw.text((140, y+8), "HP 288 G9", fill="#1A1A2E")
        draw.text((290, y+8), "计算机", fill="#1A1A2E")
        draw.text((420, y+8), "[已领用]", fill="#028090")
        draw.text((510, y+8), "¥3,200", fill="#1A1A2E")
        draw.text((620, y+8), "2026-03", fill="#E6A23C")
        draw.text((760, y+8), "2026-06", fill="#1A1A2E")
    elif row == 2:
        draw.text((35, y+8), "CE0732SZ", fill="#1A1A2E")
        draw.text((140, y+8), "Dell 7090", fill="#1A1A2E")
        draw.text((290, y+8), "台式机", fill="#1A1A2E")
        draw.text((420, y+8), "[已领用]", fill="#028090")
        draw.text((510, y+8), "¥5,600", fill="#1A1A2E")
        draw.text((620, y+8), "2025-12", fill="#DC2626")
        draw.text((760, y+8), "2025-10", fill="#1A1A2E")
    elif row == 3:
        draw.text((35, y+8), "CE0305SZ", fill="#1A1A2E")
        draw.text((140, y+8), "HP 480G2", fill="#1A1A2E")
        draw.text((290, y+8), "台式机", fill="#1A1A2E")
        draw.text((420, y+8), "[已报废]", fill="#DC2626")
        draw.text((510, y+8), "¥0", fill="#9CA3AF")
        draw.text((620, y+8), "2016-03", fill="#9CA3AF")
        draw.text((760, y+8), "-", fill="#9CA3AF")
    else:
        draw.text((35, y+8), "搜索: 输入关键词或筛选条件", fill="#9CA3AF")

# Key features
features = [
    "V  自动直线折旧，实时计算当前价值与EOL",
    "V  Checkout/Checkin闭环，领用归还全追踪",
    "V  扫码盘点，差异报告自动生成",
    "V  资产模型模板化，一键继承折旧参数",
    "V  苏州/Penang双站点数据完全隔离",
]
fy = 420
for i, f in enumerate(features):
    draw.text((25, fy+i*24), f, fill="#028090")

img_after.save(f"{out_dir}/after_system.png")
print("After image saved")
print("Done!")
