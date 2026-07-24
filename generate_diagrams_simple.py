"""
固定资产管理系统 - 使用 PIL 生成 ER 图和流程图
不需要安装 Graphviz
"""

from PIL import Image, ImageDraw, ImageFont
import os

# 创建图片输出目录
output_dir = r"F:\固定资产系统\论文图片"
os.makedirs(output_dir, exist_ok=True)

print(f"图片将保存到：{output_dir}")

# 尝试使用中文字体
try:
    font_title = ImageFont.truetype("msyh.ttc", 20)  # 微软雅黑
    font_text = ImageFont.truetype("msyh.ttc", 14)
    font_small = ImageFont.truetype("msyh.ttc", 12)
except:
    font_title = ImageFont.load_default()
    font_text = ImageFont.load_default()
    font_small = ImageFont.load_default()

# ==================== 1. ER 图 ====================
print("正在生成 ER 图...")

def create_er_diagram():
    width, height = 1400, 900
    img = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(img)
    
    # 绘制表格
    tables = [
        # 第一行
        {'name': 'sys_user\n用户表', 'x': 50, 'y': 50, 'fields': ['user_id: BIGINT', 'username: VARCHAR', 'password: VARCHAR', 'real_name: VARCHAR', 'dept_id: BIGINT', 'role: TINYINT']},
        {'name': 'sys_department\n部门表', 'x': 50, 'y': 300, 'fields': ['dept_id: BIGINT', 'dept_name: VARCHAR', 'dept_code: VARCHAR', 'manager: VARCHAR', 'phone: VARCHAR']},
        
        # 第二行
        {'name': 'asset_category\n资产分类表', 'x': 450, 'y': 50, 'fields': ['category_id: BIGINT', 'category_name: VARCHAR', 'parent_id: BIGINT', 'sort_order: INT']},
        {'name': 'asset_info\n资产信息表', 'x': 450, 'y': 250, 'fields': ['asset_id: BIGINT', 'asset_code: VARCHAR', 'asset_name: VARCHAR', 'category_id: BIGINT', 'brand: VARCHAR', 'status: TINYINT']},
        
        # 第三行
        {'name': 'asset_use_record\n领用记录表', 'x': 900, 'y': 50, 'fields': ['record_id: BIGINT', 'asset_id: BIGINT', 'user_id: BIGINT', 'department: VARCHAR', 'approve_status: TINYINT']},
        {'name': 'asset_repair_record\n维修记录表', 'x': 900, 'y': 250, 'fields': ['repair_id: BIGINT', 'asset_id: BIGINT', 'repair_reason: VARCHAR', 'repair_status: TINYINT']},
        {'name': 'asset_scrap_record\n报废记录表', 'x': 900, 'y': 450, 'fields': ['scrap_id: BIGINT', 'asset_id: BIGINT', 'scrap_reason: VARCHAR', 'scrap_type: TINYINT']},
    ]
    
    # 绘制表框
    for table in tables:
        x, y = table['x'], table['y']
        box_height = 40 + len(table['fields']) * 20
        
        # 表头
        draw.rectangle([x, y, x+200, y+35], fill='lightblue', outline='black')
        draw.text((x+10, y+10), table['name'], font=font_text, fill='black')
        
        # 字段
        for i, field in enumerate(table['fields']):
            draw.rectangle([x, y+35+i*20, x+200, y+55+i*20], fill='white', outline='black')
            draw.text((x+5, y+38+i*20), field, font=font_small, fill='black')
    
    # 绘制关系连线
    # 部门 -> 用户
    draw.line([(250, 320), (50, 320), (50, 120)], fill='gray', width=2)
    draw.text((130, 305), "1:N 所属", font=font_small, fill='gray')
    
    # 分类 -> 资产
    draw.line([(650, 70), (650, 270), (650, 270)], fill='gray', width=2)
    draw.text((560, 170), "1:N 包含", font=font_small, fill='gray')
    
    # 资产 -> 领用
    draw.line([(650, 270), (900, 270), (900, 70)], fill='gray', width=2)
    draw.text((750, 255), "1:N 领用", font=font_small, fill='gray')
    
    # 资产 -> 维修
    draw.line([(650, 270), (900, 270)], fill='gray', width=2)
    draw.text((750, 255), "1:N 维修", font=font_small, fill='gray')
    
    # 资产 -> 报废
    draw.line([(650, 270), (900, 270), (900, 470)], fill='gray', width=2)
    draw.text((750, 380), "1:N 报废", font=font_small, fill='gray')
    
    img.save(os.path.join(output_dir, '01_ER 图.png'))
    print("✓ ER 图生成完成")

create_er_diagram()

# ==================== 2. 业务流程图通用函数 ====================
def create_flowchart(title, nodes, edges, filename):
    width, height = 1200, 700
    img = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(img)
    
    # 标题
    draw.text((20, 10), title, font=font_title, fill='black')
    
    # 绘制节点
    for node in nodes:
        x, y = node['x'], node['y']
        w, h = node.get('w', 150), node.get('h', 80)
        color = node.get('color', 'lightblue')
        shape = node.get('shape', 'rect')
        
        if shape == 'rect':
            draw.rectangle([x, y, x+w, y+h], fill=color, outline='black', width=2)
        elif shape == 'diamond':
            # 菱形
            points = [(x+w/2, y), (x+w, y+h/2), (x+w/2, y+h), (x, y+h/2)]
            draw.polygon(points, fill=color, outline='black', width=2)
        elif shape == 'oval':
            draw.ellipse([x, y, x+w, y+h], fill=color, outline='black', width=2)
        
        # 文字
        text = node['text']
        draw.text((x+10, y+10), text, font=font_text, fill='black')
    
    # 绘制边
    for edge in edges:
        start, end = edge['from'], edge['to']
        label = edge.get('label', '')
        
        # 找到起始和结束节点
        start_node = next(n for n in nodes if n['id'] == start)
        end_node = next(n for n in nodes if n['id'] == end)
        
        x1, y1 = start_node['x'] + start_node.get('w', 150)/2, start_node['y'] + start_node.get('h', 80)/2
        x2, y2 = end_node['x'] + end_node.get('w', 150)/2, end_node['y'] + end_node.get('h', 80)/2
        
        # 绘制带箭头的线
        draw.line([(x1, y1), (x2, y2)], fill='blue', width=2)
        # 箭头
        arrow_len = 10
        angle = ((y2-y1)/(x2-x1)) if (x2-x1) != 0 else 999
        draw.polygon([
            (x2, y2),
            (x2 - arrow_len, y2 - arrow_len * angle),
            (x2 - arrow_len, y2 + arrow_len * angle)
        ], fill='blue')
        
        # 标签
        if label:
            draw.text(((x1+x2)/2-20, (y1+y2)/2-10), label, font=font_small, fill='blue')
    
    img.save(os.path.join(output_dir, filename))

# 资产领用流程图
print("正在生成资产领用流程图...")
nodes_use = [
    {'id': 'A', 'text': '员工\n提交领用申请', 'x': 50, 'y': 300, 'color': 'lightyellow'},
    {'id': 'B', 'text': '系统\n验证资产状态', 'x': 250, 'y': 300, 'color': 'lightgreen'},
    {'id': 'C', 'text': '管理员\n审批申请', 'x': 450, 'y': 300, 'color': 'lightyellow'},
    {'id': 'D1', 'text': '审批通过\n更新状态', 'x': 650, 'y': 200, 'color': 'lightgreen'},
    {'id': 'D2', 'text': '审批拒绝\n返回', 'x': 650, 'y': 400, 'color': 'lightcoral'},
    {'id': 'E', 'text': '员工\n领用资产', 'x': 850, 'y': 200, 'color': 'lightblue'},
    {'id': 'F', 'text': '流程结束', 'x': 1000, 'y': 300, 'shape': 'oval', 'color': 'lightgray'},
]
edges_use = [
    {'from': 'A', 'to': 'B'},
    {'from': 'B', 'to': 'C'},
    {'from': 'C', 'to': 'D1', 'label': '通过'},
    {'from': 'C', 'to': 'D2', 'label': '拒绝'},
    {'from': 'D1', 'to': 'E'},
    {'from': 'D2', 'to': 'F'},
    {'from': 'E', 'to': 'F'},
]
create_flowchart("图 5-2 资产领用业务流程图", nodes_use, edges_use, '02_资产领用流程图.png')
print("✓ 资产领用流程图生成完成")

# 资产维修流程图
print("正在生成资产维修流程图...")
nodes_repair = [
    {'id': 'A', 'text': '使用人\n提交报修申请', 'x': 50, 'y': 300, 'color': 'lightyellow'},
    {'id': 'B', 'text': '系统\n更新状态为维修中', 'x': 250, 'y': 300, 'color': 'lightgreen'},
    {'id': 'C', 'text': '维修人员\n接收任务', 'x': 450, 'y': 300, 'color': 'lightyellow'},
    {'id': 'D', 'text': '进行维修', 'x': 650, 'y': 300, 'color': 'lightblue'},
    {'id': 'E1', 'text': '维修完成\n记录费用', 'x': 850, 'y': 200, 'color': 'lightgreen'},
    {'id': 'E2', 'text': '无法修复\n申请报废', 'x': 850, 'y': 400, 'color': 'lightcoral'},
    {'id': 'F', 'text': '流程结束', 'x': 1000, 'y': 300, 'shape': 'oval', 'color': 'lightgray'},
]
edges_repair = [
    {'from': 'A', 'to': 'B'},
    {'from': 'B', 'to': 'C'},
    {'from': 'C', 'to': 'D'},
    {'from': 'D', 'to': 'E1', 'label': '成功'},
    {'from': 'D', 'to': 'E2', 'label': '失败'},
    {'from': 'E1', 'to': 'F'},
    {'from': 'E2', 'to': 'F'},
]
create_flowchart("图 5-3 资产维修业务流程图", nodes_repair, edges_repair, '03_资产维修流程图.png')
print("✓ 资产维修流程图生成完成")

# 资产报废流程图
print("正在生成资产报废流程图...")
nodes_scrap = [
    {'id': 'A', 'text': '申请人\n提交报废申请', 'x': 50, 'y': 300, 'color': 'lightyellow'},
    {'id': 'B', 'text': '选择类型\n填写原值/残值', 'x': 250, 'y': 300, 'color': 'lightblue'},
    {'id': 'C', 'text': '管理员\n审批申请', 'x': 450, 'y': 300, 'color': 'lightyellow'},
    {'id': 'D1', 'text': '审批通过\n更新为已报废', 'x': 650, 'y': 200, 'color': 'lightgreen'},
    {'id': 'D2', 'text': '审批拒绝\n返回', 'x': 650, 'y': 400, 'color': 'lightcoral'},
    {'id': 'E', 'text': '资产归档', 'x': 850, 'y': 200, 'color': 'lightblue'},
    {'id': 'F', 'text': '流程结束', 'x': 1000, 'y': 300, 'shape': 'oval', 'color': 'lightgray'},
]
edges_scrap = [
    {'from': 'A', 'to': 'B'},
    {'from': 'B', 'to': 'C'},
    {'from': 'C', 'to': 'D1', 'label': '通过'},
    {'from': 'C', 'to': 'D2', 'label': '拒绝'},
    {'from': 'D1', 'to': 'E'},
    {'from': 'D2', 'to': 'F'},
    {'from': 'E', 'to': 'F'},
]
create_flowchart("图 5-4 资产报废业务流程图", nodes_scrap, edges_scrap, '04_资产报废流程图.png')
print("✓ 资产报废流程图生成完成")

# ==================== 3. 系统架构图 ====================
print("正在生成系统架构图...")

def create_architecture():
    width, height = 800, 600
    img = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(img)
    
    # 标题
    draw.text((20, 10), "图 4-1 系统技术架构图", font=font_title, fill='black')
    
    layers = [
        {'name': '表现层 (Presentation Layer)\nVue 3 + Element Plus + Axios', 'y': 50, 'color': 'lightblue'},
        {'name': '控制层 (Controller Layer)\nSpring Boot + RESTful API', 'y': 180, 'color': 'lightgreen'},
        {'name': '业务层 (Service Layer)\n事务管理 + 业务逻辑 + 数据校验', 'y': 310, 'color': 'lightyellow'},
        {'name': '持久层 (Persistence Layer)\nMyBatis-Plus + MySQL', 'y': 440, 'color': 'lightcoral'},
    ]
    
    for layer in layers:
        x, y = 100, layer['y']
        w, h = 600, 80
        
        # 3D 效果框
        for i in range(10, 0, -1):
            draw.rectangle([x+i, y+i, x+w+i, y+h+i], fill=layer['color'], outline='black')
        
        draw.text((x+20, y+25), layer['name'], font=font_text, fill='black')
    
    # 箭头
    for i in range(3):
        y = 140 + i * 130
        draw.polygon([(390, y), (385, y+10), (400, y+10)], fill='blue')
        draw.line([(395, 130+i*130), (395, 140+i*130)], fill='blue', width=3)
    
    img.save(os.path.join(output_dir, '05_系统架构图.png'))
    print("✓ 系统架构图生成完成")

create_architecture()

print("\n" + "="*50)
print("✅ 图片生成完成！")
print("="*50)
print(f"\n图片保存位置：{output_dir}")
print("\n生成的文件：")
print("  1. 01_ER 图.png")
print("  2. 02_资产领用流程图.png")
print("  3. 03_资产维修流程图.png")
print("  4. 04_资产报废流程图.png")
print("  5. 05_系统架构图.png")
print("\n提示：将这些图片插入到 Word 文档的对应章节即可！")
