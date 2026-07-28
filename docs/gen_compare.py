from PIL import Image, ImageDraw

W, H = 960, 540

def make_comparison_img(left_title, left_items, left_color, right_title, right_items, right_color, filename):
    img = Image.new("RGB", (W, H), "#F8F9FA")
    d = ImageDraw.Draw(img)
    # Left panel
    d.rectangle([0, 0, 479, 40], fill=left_color)
    d.text((15, 8), left_title, fill="white")
    for i, item in enumerate(left_items):
        y = 55 + i * 55
        d.rectangle([10, y, 469, y+48], fill="white", outline="#E5E7EB")
        d.rectangle([10, y, 15, y+48], fill=left_color)
        d.text((25, y+10), item[0], fill="#111827")
        d.text((25, y+28), item[1], fill="#6B7280")
    # Right panel
    d.rectangle([481, 0, W, 40], fill=right_color)
    d.text((496, 8), right_title, fill="white")
    for i, item in enumerate(right_items):
        y = 55 + i * 55
        d.rectangle([491, y, 950, y+48], fill="white", outline="#E5E7EB")
        d.rectangle([491, y, 496, y+48], fill=right_color)
        d.text((506, y+10), item[0], fill="#111827")
        d.text((506, y+28), item[1], fill="#6B7280")
    img.save(f"E:/it-asset-system/docs/{filename}")
    print(f"Saved {filename}")

# Pain points vs Solutions
make_comparison_img(
    "BEFORE: Excel管理", 
    [("版本混乱","多版本并存，无法确认最新数据"),
     ("数据孤岛","各部门表格各自为政，信息割裂"),
     ("盘点低效","人工核对耗时数周，账实不符"),
     ("缺乏追溯","资产变动无记录，责任难追踪")],
    "#DC2626",
    "AFTER: 智能系统",
    [("一物一码","每件资产唯一二维码/标签，扫码即查"),
     ("全生命周期","采购-入库-领用-调拨-维修-报废全闭环"),
     ("智能盘点","手机扫码采集，实时同步，分钟级完成"),
     ("数据驾驶舱","多维度报表，资产分布效率一目了然")],
    "#028090",
    "compare_pain.png"
)

# Comparison table visual
make_comparison_img(
    "改善前（Excel）", 
    [("数据录入","手工录入，易错漏"),
     ("数据一致性","多版本混乱难管理"),
     ("盘点效率","耗时数周"),
     ("资产追溯","变动无记录"),
     ("报表统计","手工汇总，按天计"),
     ("决策支持","凭经验判断")],
    "#DC2626",
    "改善后（AI系统）",
    [("数据录入","扫码自动采集，准确高效"),
     ("数据一致性","云端统一，实时同步"),
     ("盘点效率","分钟级完成"),
     ("资产追溯","全流程留痕可查"),
     ("报表统计","自动生成，分钟级"),
     ("决策支持","数据驾驶舱可视化")],
    "#028090",
    "compare_table.png"
)

print("All images generated")
