"""
固定资产管理系统 - ER 图和流程图生成脚本
运行此脚本将生成所有论文需要的图片
"""

from graphviz import Digraph, Source
import os

# 创建图片输出目录
output_dir = r"F:\固定资产系统\论文图片"
os.makedirs(output_dir, exist_ok=True)

print(f"图片将保存到：{output_dir}")

# ==================== 1. ER 图 ====================
print("正在生成 ER 图...")

er_dot = """
digraph ER_Diagram {
    rankdir=TB;
    node [shape=box, style=filled, fillcolor=lightblue, fontname="Microsoft YaHei", fontsize=12];
    edge [fontname="Microsoft YaHei", fontsize=10];
    
    // 用户表
    sys_user [label="sys_user\\n用户表|{user_id: BIGINT\\nusername: VARCHAR\\npassword: VARCHAR\\nreal_name: VARCHAR\\ndept_id: BIGINT\\nrole: TINYINT\\nstatus: TINYINT}", shape=Mrecord];
    
    // 部门表
    sys_department [label="sys_department\\n部门表|{dept_id: BIGINT\\ndept_name: VARCHAR\\ndept_code: VARCHAR\\nmanager: VARCHAR\\nphone: VARCHAR\\nstatus: TINYINT}", shape=Mrecord];
    
    // 资产分类表
    asset_category [label="asset_category\\n资产分类表|{category_id: BIGINT\\ncategory_name: VARCHAR\\nparent_id: BIGINT\\nsort_order: INT}", shape=Mrecord];
    
    // 资产信息表
    asset_info [label="asset_info\\n资产信息表|{asset_id: BIGINT\\nasset_code: VARCHAR\\nasset_name: VARCHAR\\ncategory_id: BIGINT\\nbrand: VARCHAR\\nmodel: VARCHAR\\npurchase_price: DECIMAL\\nstatus: TINYINT}", shape=Mrecord];
    
    // 领用记录表
    asset_use_record [label="asset_use_record\\n领用记录表|{record_id: BIGINT\\nasset_id: BIGINT\\nuser_id: BIGINT\\ndepartment: VARCHAR\\ncontact_person: VARCHAR\\napprove_status: TINYINT}", shape=Mrecord];
    
    // 维修记录表
    asset_repair_record [label="asset_repair_record\\n维修记录表|{repair_id: BIGINT\\nasset_id: BIGINT\\nrepair_reason: VARCHAR\\nrepair_status: TINYINT\\napply_user: VARCHAR}", shape=Mrecord];
    
    // 报废记录表
    asset_scrap_record [label="asset_scrap_record\\n报废记录表|{scrap_id: BIGINT\\nasset_id: BIGINT\\nscrap_reason: VARCHAR\\nscrap_type: TINYINT\\napprove_status: TINYINT}", shape=Mrecord];
    
    // 关系
    sys_department -> sys_user [label="1:N\\n所属", dir=both];
    asset_category -> asset_info [label="1:N\\n包含", dir=both];
    asset_info -> asset_use_record [label="1:N\\n领用", dir=both];
    asset_info -> asset_repair_record [label="1:N\\n维修", dir=both];
    asset_info -> asset_scrap_record [label="1:N\\n报废", dir=both];
    sys_user -> asset_use_record [label="1:N\\n申请", dir=both];
}
"""

er_graph = Source(er_dot, format='png')
er_graph.render(filename=os.path.join(output_dir, '01_ER 图'), cleanup=True)
print("✓ ER 图生成完成")

# ==================== 2. 资产领用业务流程图 ====================
print("正在生成资产领用业务流程图...")

use_flow = Digraph('use_flow', format='png')
use_flow.attr(rankdir='LR', fontname='Microsoft YaHei')
use_flow.attr('node', shape='box', style='filled', fillcolor='lightblue', fontname='Microsoft YaHei')
use_flow.attr('edge', fontname='Microsoft YaHei')

use_flow.node('A', '员工\n提交领用申请', fillcolor='lightyellow')
use_flow.node('B', '系统\n验证资产状态', fillcolor='lightgreen')
use_flow.node('C', '管理员\n审批申请', fillcolor='lightyellow')
use_flow.node('D1', '审批通过\n更新资产状态为已领用', fillcolor='lightgreen')
use_flow.node('D2', '审批拒绝\n返回申请人', fillcolor='lightcoral')
use_flow.node('E', '员工\n领用资产', fillcolor='lightblue')
use_flow.node('F', '员工\n申请归还', fillcolor='lightyellow')
use_flow.node('G', '系统\n更新资产状态为未领用', fillcolor='lightgreen')
use_flow.node('H', '流程结束', shape='oval', fillcolor='lightgray')

use_flow.edge('A', 'B', '提交申请')
use_flow.edge('B', 'C', '资产可用')
use_flow.edge('C', 'D1', '通过')
use_flow.edge('C', 'D2', '拒绝')
use_flow.edge('D1', 'E', '通知领用')
use_flow.edge('D2', 'H', '结束')
use_flow.edge('E', 'F', '使用完毕')
use_flow.edge('F', 'G', '确认归还')
use_flow.edge('G', 'H', '完成')

use_flow.render(filename='02_资产领用流程图', directory=output_dir, cleanup=True)
print("✓ 资产领用流程图生成完成")

# ==================== 3. 资产维修业务流程图 ====================
print("正在生成资产维修业务流程图...")

repair_flow = Digraph('repair_flow', format='png')
repair_flow.attr(rankdir='LR', fontname='Microsoft YaHei')
repair_flow.attr('node', shape='box', style='filled', fillcolor='lightblue', fontname='Microsoft YaHei')
repair_flow.attr('edge', fontname='Microsoft YaHei')

repair_flow.node('A', '使用人\n提交报修申请', fillcolor='lightyellow')
repair_flow.node('B', '系统\n更新资产状态为维修中', fillcolor='lightgreen')
repair_flow.node('C', '维修人员\n接收维修任务', fillcolor='lightyellow')
repair_flow.node('D', '进行维修', fillcolor='lightblue')
repair_flow.node('E1', '维修完成\n记录维修费用', fillcolor='lightgreen')
repair_flow.node('E2', '无法修复\n申请报废', fillcolor='lightcoral')
repair_flow.node('F', '系统\n更新资产状态为未领用', fillcolor='lightgreen')
repair_flow.node('G', '流程结束', shape='oval', fillcolor='lightgray')

repair_flow.edge('A', 'B', '提交报修')
repair_flow.edge('B', 'C', '状态更新')
repair_flow.edge('C', 'D', '开始维修')
repair_flow.edge('D', 'E1', '修复成功')
repair_flow.edge('D', 'E2', '修复失败')
repair_flow.edge('E1', 'F', '记录归档')
repair_flow.edge('E2', 'G', '转报废流程')
repair_flow.edge('F', 'G', '完成')

repair_flow.render(filename='03_资产维修流程图', directory=output_dir, cleanup=True)
print("✓ 资产维修流程图生成完成")

# ==================== 4. 资产报废业务流程图 ====================
print("正在生成资产报废业务流程图...")

scrap_flow = Digraph('scrap_flow', format='png')
scrap_flow.attr(rankdir='LR', fontname='Microsoft YaHei')
scrap_flow.attr('node', shape='box', style='filled', fillcolor='lightblue', fontname='Microsoft YaHei')
scrap_flow.attr('edge', fontname='Microsoft YaHei')

scrap_flow.node('A', '申请人\n提交报废申请', fillcolor='lightyellow')
scrap_flow.node('B', '选择报废类型\n填写原值/残值', fillcolor='lightblue')
scrap_flow.node('C', '管理员\n审批申请', fillcolor='lightyellow')
scrap_flow.node('D1', '审批通过\n更新资产状态为已报废', fillcolor='lightgreen')
scrap_flow.node('D2', '审批拒绝\n返回申请人', fillcolor='lightcoral')
scrap_flow.node('E', '资产归档\n记录报废信息', fillcolor='lightblue')
scrap_flow.node('F', '流程结束', shape='oval', fillcolor='lightgray')

scrap_flow.edge('A', 'B', '提交申请')
scrap_flow.edge('B', 'C', '填写完整')
scrap_flow.edge('C', 'D1', '通过')
scrap_flow.edge('C', 'D2', '拒绝')
scrap_flow.edge('D1', 'E', '状态更新')
scrap_flow.edge('D2', 'F', '结束')
scrap_flow.edge('E', 'F', '完成')

scrap_flow.render(filename='04_资产报废流程图', directory=output_dir, cleanup=True)
print("✓ 资产报废流程图生成完成")

# ==================== 5. 系统架构图 ====================
print("正在生成系统架构图...")

arch_flow = Digraph('architecture', format='png')
arch_flow.attr(rankdir='TB', fontname='Microsoft YaHei')
arch_flow.attr('node', shape='box', style='filled', fontname='Microsoft YaHei')
arch_flow.attr('edge', fontname='Microsoft YaHei')

# 表现层
arch_flow.node('layer1', '表现层 (Presentation Layer)\nVue 3 + Element Plus + Axios', 
               fillcolor='lightblue', shape='box3d')

# 控制层
arch_flow.node('layer2', '控制层 (Controller Layer)\nSpring Boot + RESTful API', 
               fillcolor='lightgreen', shape='box3d')

# 业务层
arch_flow.node('layer3', '业务层 (Service Layer)\n事务管理 + 业务逻辑 + 数据校验', 
               fillcolor='lightyellow', shape='box3d')

# 持久层
arch_flow.node('layer4', '持久层 (Persistence Layer)\nMyBatis-Plus + MySQL', 
               fillcolor='lightcoral', shape='box3d')

arch_flow.edge('layer1', 'layer2', 'HTTP/HTTPS')
arch_flow.edge('layer2', 'layer3', '调用')
arch_flow.edge('layer3', 'layer4', '数据访问')

arch_flow.render(filename='05_系统架构图', directory=output_dir, cleanup=True)
print("✓ 系统架构图生成完成")

# ==================== 6. 用例图 ====================
print("正在生成用例图...")

usecase_dot = """
digraph UseCase {
    rankdir=LR;
    node [shape=ellipse, style=filled, fillcolor=lightyellow, fontname="Microsoft YaHei", fontsize=11];
    edge [fontname="Microsoft YaHei", fontsize=9];
    
    // 参与者
    subgraph cluster_actors {
        label="参与者";
        style=dashed;
        color=gray;
        
        employee [label="普通员工", shape=box, fillcolor=lightblue];
        admin [label="管理员", shape=box, fillcolor=lightgreen];
        system [label="系统", shape=box, fillcolor=lightgray];
    }
    
    // 员工用例
    subgraph cluster_employee {
        label="员工功能";
        style=dashed;
        color=blue;
        
        view_asset [label="查看资产"];
        apply_use [label="申请领用"];
        apply_repair [label="申请报修"];
        return_asset [label="归还资产"];
    }
    
    // 管理员用例
    subgraph cluster_admin {
        label="管理员功能";
        style=dashed;
        color=green;
        
        approve_use [label="审批领用"];
        approve_scrap [label="审批报废"];
        manage_asset [label="资产管理"];
        manage_user [label="用户管理"];
        view_report [label="统计报表"];
    }
    
    // 系统用例
    subgraph cluster_system {
        label="系统功能";
        style=dashed;
        color=gray;
        
        log_record [label="日志记录"];
        data_backup [label="数据备份"];
        auth_control [label="权限控制"];
    }
    
    // 关系
    employee -> view_asset;
    employee -> apply_use;
    employee -> apply_repair;
    employee -> return_asset;
    
    admin -> approve_use;
    admin -> approve_scrap;
    admin -> manage_asset;
    admin -> manage_user;
    admin -> view_report;
    
    system -> log_record;
    system -> data_backup;
    system -> auth_control;
}
"""

usecase_graph = Source(usecase_dot, format='png')
usecase_graph.render(filename=os.path.join(output_dir, '06_用例图'), cleanup=True)
print("✓ 用例图生成完成")

# ==================== 7. 数据流图 DFD-顶层 ====================
print("正在生成数据流图（顶层）...")

dfd_top = Digraph('dfd_top', format='png')
dfd_top.attr(rankdir='LR', fontname='Microsoft YaHei')
dfd_top.attr('node', shape='circle', style='filled', fillcolor='lightblue', fontname='Microsoft YaHei')
dfd_top.attr('edge', fontname='Microsoft YaHei')

dfd_top.node('P0', 'IT 固定资产\n管理系统', shape='circle', style='filled', fillcolor='lightyellow', width='2')
dfd_top.node('E1', '员工', shape='box', fillcolor='lightgreen')
dfd_top.node('E2', '管理员', shape='box', fillcolor='lightcoral')
dfd_top.node('D1', 'MySQL\n数据库', shape='cylinder', fillcolor='lightgray')

dfd_top.edge('E1', 'P0', '领用/报修申请')
dfd_top.edge('E2', 'P0', '审批/管理操作')
dfd_top.edge('P0', 'E1', '申请结果通知')
dfd_top.edge('P0', 'E2', '待办事项提醒')
dfd_top.edge('P0', 'D1', '数据读写')
dfd_top.edge('D1', 'P0', '查询结果')

dfd_top.render(filename='07_DFD 顶层图', directory=output_dir, cleanup=True)
print("✓ DFD 顶层图生成完成")

# ==================== 8. 数据流图 DFD-第一层 ====================
print("正在生成数据流图（第一层）...")

dfd_l1 = Digraph('dfd_level1', format='png')
dfd_l1.attr(rankdir='TB', fontname='Microsoft YaHei')
dfd_l1.attr('node', shape='box', style='filled', fontname='Microsoft YaHei')
dfd_l1.attr('edge', fontname='Microsoft YaHei')

# 外部实体
dfd_l1.node('E1', '员工', fillcolor='lightgreen')
dfd_l1.node('E2', '管理员', fillcolor='lightcoral')

# 处理过程
dfd_l1.node('P1', 'P1: 用户认证', fillcolor='lightyellow')
dfd_l1.node('P2', 'P2: 资产管理', fillcolor='lightblue')
dfd_l1.node('P3', 'P3: 领用管理', fillcolor='lightblue')
dfd_l1.node('P4', 'P4: 维修管理', fillcolor='lightblue')

# 数据存储
dfd_l1.node('D1', 'D1: 资产表', shape='cylinder', fillcolor='lightgray')
dfd_l1.node('D2', 'D2: 领用表', shape='cylinder', fillcolor='lightgray')
dfd_l1.node('D3', 'D3: 维修表', shape='cylinder', fillcolor='lightgray')

dfd_l1.edge('E1', 'P1', '登录信息')
dfd_l1.edge('E2', 'P1', '登录信息')
dfd_l1.edge('P1', 'E1', '认证结果')
dfd_l1.edge('P1', 'E2', '认证结果')

dfd_l1.edge('E1', 'P3', '领用申请')
dfd_l1.edge('E1', 'P4', '报修申请')
dfd_l1.edge('E2', 'P2', '资产管理操作')
dfd_l1.edge('E2', 'P3', '审批操作')

dfd_l1.edge('P2', 'D1', '读写')
dfd_l1.edge('P3', 'D2', '读写')
dfd_l1.edge('P4', 'D3', '读写')
dfd_l1.edge('D1', 'P2', '查询')
dfd_l1.edge('D2', 'P3', '查询')
dfd_l1.edge('D3', 'P4', '查询')

dfd_l1.render(filename='08_DFD 第一层图', directory=output_dir, cleanup=True)
print("✓ DFD 第一层图生成完成")

print("\n" + "="*50)
print("✅ 所有图片生成完成！")
print("="*50)
print(f"\n图片保存位置：{output_dir}")
print("\n生成的文件列表：")
print("  1. 01_ER 图.png")
print("  2. 02_资产领用流程图.png")
print("  3. 03_资产维修流程图.png")
print("  4. 04_资产报废流程图.png")
print("  5. 05_系统架构图.png")
print("  6. 06_用例图.png")
print("  7. 07_DFD 顶层图.png")
print("  8. 08_DFD 第一层图.png")
print("\n提示：将这些图片插入到 Word 文档的对应章节即可！")
