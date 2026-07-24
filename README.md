# IT 固定资产管理系统

一个基于 Spring Boot + Vue 3 的 IT 固定资产管理系统。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0+
- **JDK**: 17

### 前端
- **框架**: Vue 3.2
- **UI 库**: Element Plus 2.13
- **路由**: Vue Router 4.6
- **HTTP**: Axios 1.13

## 快速开始

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source F:\固定资产系统\database\init.sql
```

初始化脚本会创建：
- 数据库 `it_asset_manage`
- 6 张表（用户、分类、资产、领用记录、维修记录、报废记录）
- 默认管理员账号：`admin` / `123456`
- 测试资产数据

### 2. 后端启动

```bash
cd F:\固定资产系统\it-asset-system

# 使用 Maven 启动
mvnw spring-boot:run

# 或者打包后运行
mvnw clean package
java -jar target\it-asset-system-0.0.1-SNAPSHOT.jar
```

后端服务地址：http://localhost:8080/asset

### 3. 前端启动

```bash
cd F:\固定资产系统\it-asset-frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run serve
```

前端访问地址：http://localhost:8081

## 功能清单

### ✅ 已实现功能
- [x] 用户登录/登出
- [x] 资产入库（新增）
- [x] 资产列表（分页查询）
- [x] 资产编辑
- [x] 资产删除
- [x] 资产搜索（按名称、分类、状态）
- [x] 资产分类管理
- [x] 数据导出（CSV）

### 📋 待实现功能
- [ ] 资产领用/归还
- [ ] 资产维修管理
- [ ] 资产报废管理
- [ ] 用户管理
- [ ] 统计报表
- [ ] 资产二维码生成
- [ ] 批量导入/导出

## 数据库表结构

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户表 |
| asset_category | 资产分类表 |
| asset_info | 资产信息表 |
| asset_use_record | 资产领用记录表 |
| asset_repair_record | 资产维修记录表 |
| asset_scrap_record | 资产报废记录表 |

## API 接口

### 认证相关
- `POST /asset/login` - 用户登录
- `GET /asset/login/info` - 获取当前用户信息

### 资产管理
- `GET /asset/assetInfo/page` - 分页查询资产
- `GET /asset/assetInfo/list` - 查询所有资产
- `GET /asset/assetInfo/detail` - 资产详情
- `POST /asset/assetInfo/save` - 新增资产
- `POST /asset/assetInfo/update` - 更新资产
- `POST /asset/assetInfo/delete` - 删除资产

### 分类管理
- `GET /asset/category/list` - 查询所有分类
- `GET /asset/category/parent` - 查询一级分类
- `POST /asset/category/save` - 新增分类
- `POST /asset/category/update` - 更新分类
- `POST /asset/category/delete` - 删除分类

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |

## 注意事项

1. **数据库配置**: 修改 `application.yml` 中的数据库连接信息
2. **端口占用**: 后端默认 8080，前端默认 8081，如有冲突请修改
3. **跨域问题**: 后端已配置 CORS，允许前端跨域访问
4. **密码加密**: 当前使用 MD5 加密，生产环境建议使用 BCrypt

## 项目结构

```
固定资产系统/
├── database/              # 数据库脚本
│   └── init.sql
├── it-asset-system/       # 后端（Spring Boot）
│   ├── src/main/java/
│   │   └── com/asset/itassetsystem/
│   │       ├── Controller/
│   │       ├── Service/
│   │       ├── entity/
│   │       ├── mapper/
│   │       └── dto/
│   └── src/main/resources/
│       └── application.yml
└── it-asset-frontend/     # 前端（Vue 3）
    ├── src/
    │   ├── views/
    │   ├── router/
    │   └── utils/
    └── package.json
```

## 开发计划

### v1.0（当前版本）
- 基础 CRUD 功能
- 用户登录
- 资产分类

### v1.1
- 资产领用/归还流程
- 资产状态变更

### v1.2
- 维修管理
- 报废管理

### v2.0
- 统计报表
- 数据可视化
- 二维码生成

---

**开发时间**: 2026 年 3 月
**版本**: v1.0.0
