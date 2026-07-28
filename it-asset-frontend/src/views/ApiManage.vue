<template>
  <div class="page-container">
    <div class="header-title">接口管理</div>

    <!-- API 概览卡片 -->
    <div class="search-section" style="padding:12px 16px;display:flex;gap:24px;align-items:center;flex-wrap:wrap;">
      <el-input v-model="searchKeyword" placeholder="搜索接口路径或说明" clearable style="width:300px" prefix-icon="Search" />
      <el-select v-model="filterModule" placeholder="按模块筛选" clearable style="width:180px" @change="onFilter">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <span style="color:#909399;font-size:13px;margin-left:auto;">
        共 <b>{{ filteredApis.length }}</b> 个接口 / {{ apis.length }} 总计
      </span>
    </div>

    <div class="table-section">
      <el-table :data="filteredApis" border stripe row-key="path" style="width:100%">
        <el-table-column label="方法" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="methodColor(row.method)" size="small" effect="dark">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <code style="font-size:13px;color:#409EFF;">{{ row.path }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="160" align="center" />
        <el-table-column prop="desc" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="参数" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.params" style="font-size:12px;color:#909399;">{{ row.params }}</span>
            <span v-else style="color:#c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="认证" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.auth ? 'warning' : 'success'" size="small" effect="plain">
              {{ row.auth ? '需登录' : '公开' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const searchKeyword = ref('')
const filterModule = ref('')

const apis = ref([
  // 认证
  { method: 'POST', path: '/asset/login', module: '认证', desc: '用户登录', params: 'Body: username, password', auth: false },
  // 首页
  { method: 'GET', path: '/asset/home/stats', module: '仪表盘', desc: '基础统计', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/home/dashboard', module: '仪表盘', desc: '完整仪表盘数据（含折旧/逾期/消耗趋势）', params: 'Query: site', auth: true },
  // 资产信息
  { method: 'GET', path: '/asset/assetInfo/page', module: '资产管理', desc: '分页查询资产（支持多条件筛选排序）', params: 'Query: current,size,keyword,status,department,modelId,statusLabelId,sortColumn,sortOrder...', auth: true },
  { method: 'GET', path: '/asset/assetInfo/list', module: '资产管理', desc: '查询全部资产', params: '-', auth: true },
  { method: 'GET', path: '/asset/assetInfo/detail', module: '资产管理', desc: '查资产详情', params: 'Query: assetId', auth: true },
  { method: 'POST', path: '/asset/assetInfo/save', module: '资产管理', desc: '新增资产（自动算折旧/EOL/当前价值）', params: 'Body: AssetInfo JSON', auth: true },
  { method: 'POST', path: '/asset/assetInfo/update', module: '资产管理', desc: '更新资产', params: 'Body: AssetInfo JSON', auth: true },
  { method: 'POST', path: '/asset/assetInfo/delete', module: '资产管理', desc: '删除资产', params: 'Query: assetId', auth: true },
  { method: 'POST', path: '/asset/assetInfo/batchSave', module: '资产管理', desc: '批量导入资产', params: 'Body: [AssetInfo]', auth: true },
  { method: 'POST', path: '/asset/assetInfo/batchUpdate', module: '资产管理', desc: '批量更新资产字段', params: 'Body: BatchUpdateDTO', auth: true },
  { method: 'GET', path: '/asset/assetInfo/change-log', module: '资产管理', desc: '资产变更历史', params: 'Query: assetId', auth: true },
  // 分类
  { method: 'GET', path: '/asset/category/list', module: '资产分类', desc: '分类列表', params: '-', auth: true },
  { method: 'POST', path: '/asset/category/save', module: '资产分类', desc: '新增分类', params: '-', auth: true },
  { method: 'POST', path: '/asset/category/update', module: '资产分类', desc: '更新分类', params: '-', auth: true },
  { method: 'POST', path: '/asset/category/delete', module: '资产分类', desc: '删除分类', params: 'Query: categoryId', auth: true },
  // 资产模型
  { method: 'GET', path: '/asset/assetModel/page', module: '资产模型', desc: '分页查询模型', params: 'Query: current,size,modelName,categoryId,site', auth: true },
  { method: 'GET', path: '/asset/assetModel/list', module: '资产模型', desc: '查询全部模型', params: 'Query: site', auth: true },
  { method: 'POST', path: '/asset/assetModel/save', module: '资产模型', desc: '新增模型', params: 'Body: AssetModel JSON', auth: true },
  { method: 'POST', path: '/asset/assetModel/update', module: '资产模型', desc: '更新模型', params: 'Body: AssetModel JSON', auth: true },
  { method: 'POST', path: '/asset/assetModel/delete', module: '资产模型', desc: '删除模型', params: 'Query: modelId', auth: true },
  // 状态标签
  { method: 'GET', path: '/asset/statusLabel/list', module: '状态标签', desc: '查询全部状态标签', params: 'Query: site', auth: true },
  { method: 'POST', path: '/asset/statusLabel/save', module: '状态标签', desc: '新增状态标签', params: 'Body: StatusLabel JSON', auth: true },
  { method: 'POST', path: '/asset/statusLabel/update', module: '状态标签', desc: '更新状态标签', params: 'Body: StatusLabel JSON', auth: true },
  { method: 'POST', path: '/asset/statusLabel/delete', module: '状态标签', desc: '删除状态标签', params: 'Query: statusLabelId', auth: true },
  // 领用归还
  { method: 'POST', path: '/asset/use/apply', module: '领用管理', desc: '领用申请（支持预期归还日期）', params: 'Body: UseApplyDTO', auth: true },
  { method: 'POST', path: '/asset/use/approve', module: '领用管理', desc: '审批领用', params: 'Query: recordId, approved', auth: true },
  { method: 'POST', path: '/asset/use/return', module: '领用管理', desc: '归还资产（自动算逾期）', params: 'Query: assetId', auth: true },
  { method: 'GET', path: '/asset/use/page', module: '领用管理', desc: '分页查询领用记录（支持逾期筛选）', params: 'Query: current,size,overdue,status...', auth: true },
  { method: 'GET', path: '/asset/use/pending', module: '领用管理', desc: '待审批列表', params: '-', auth: true },
  { method: 'POST', path: '/asset/return/submit', module: '归还管理', desc: '提交归还申请', params: 'Body: ReturnRecord', auth: true },
  { method: 'POST', path: '/asset/return/approve', module: '归还管理', desc: '审批归还', params: 'Query: returnId, approved', auth: true },
  // 维修/报废/调拨
  { method: 'POST', path: '/asset/repair/apply', module: '维修管理', desc: '报修申请', params: 'Body: RepairApplyDTO', auth: true },
  { method: 'GET', path: '/asset/repair/page', module: '维修管理', desc: '分页查询维修', params: 'Query: current,size,status,site', auth: true },
  { method: 'POST', path: '/asset/scrap/apply', module: '报废管理', desc: '报废申请', params: 'Body: ScrapApplyDTO', auth: true },
  { method: 'POST', path: '/asset/transfer/apply', module: '调拨管理', desc: '调拨申请', params: 'Body: TransferApplyDTO', auth: true },
  // 盘点
  { method: 'POST', path: '/asset/inventory/create', module: '资产盘点', desc: '创建盘点任务', params: 'Body: InventoryCreateDTO', auth: true },
  { method: 'POST', path: '/asset/inventory/check', module: '资产盘点', desc: '执行盘点（含实际位置/差异类型）', params: 'Query: detailId,status,actualLocation,differenceType', auth: true },
  { method: 'POST', path: '/asset/inventory/finish', module: '资产盘点', desc: '完成盘点', params: 'Query: inventoryId', auth: true },
  { method: 'GET', path: '/asset/inventory/page', module: '资产盘点', desc: '分页查询', params: 'Query: pageNum,pageSize,status,site', auth: true },
  { method: 'GET', path: '/asset/inventory/report', module: '资产盘点', desc: '差异汇总报告', params: 'Query: inventoryId', auth: true },
  // 耗材
  { method: 'GET', path: '/asset/consumable/list', module: '耗材管理', desc: '列表', params: 'Query: keyword,site', auth: true },
  { method: 'POST', path: '/asset/consumable/save', module: '耗材管理', desc: '新增耗材', params: 'Body: Consumable', auth: true },
  { method: 'POST', path: '/asset/consumable/stock-in', module: '耗材管理', desc: '入库', params: 'Query: consumableId,quantity', auth: true },
  { method: 'POST', path: '/asset/consumable/stock-out', module: '耗材管理', desc: '出库', params: 'Query: consumableId,quantity', auth: true },
  { method: 'GET', path: '/asset/consumable/low-stock', module: '耗材管理', desc: '低库存列表', params: '-', auth: true },
  { method: 'GET', path: '/asset/consumable/alerts', module: '耗材管理', desc: '低库存告警（含建议补充量）', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/consumable/trend', module: '耗材管理', desc: '消耗趋势（按月统计）', params: 'Query: months', auth: true },
  { method: 'GET', path: '/asset/consumable/records', module: '耗材管理', desc: '出入库记录', params: 'Query: consumableId', auth: true },
  // 许可证
  { method: 'GET', path: '/asset/license/list', module: '许可证', desc: '列表', params: 'Query: keyword,site', auth: true },
  { method: 'POST', path: '/asset/license/save', module: '许可证', desc: '新增', params: '-', auth: true },
  // 部门/地点
  { method: 'GET', path: '/asset/department/list', module: '部门管理', desc: '列表', params: '-', auth: true },
  { method: 'POST', path: '/asset/department/save', module: '部门管理', desc: '新增部门', params: '-', auth: true },
  { method: 'GET', path: '/asset/storage-location/list', module: '存放地点', desc: '列表', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/storage-location/tree', module: '存放地点', desc: '树形结构', params: 'Query: site', auth: true },
  { method: 'POST', path: '/asset/storage-location/save', module: '存放地点', desc: '新增（支持parentId层级）', params: 'Body: StorageLocation', auth: true },
  // 用户管理
  { method: 'GET', path: '/asset/user/list', module: '用户管理', desc: '分页查询用户', params: 'Query: current,size,keyword,site', auth: true },
  { method: 'GET', path: '/asset/user/all', module: '用户管理', desc: '全部用户列表', params: '-', auth: true },
  { method: 'POST', path: '/asset/user/save', module: '用户管理', desc: '新增用户', params: 'Body: User JSON', auth: true },
  // 用户组ACL
  { method: 'GET', path: '/asset/group/list', module: '用户组', desc: '用户组列表（含成员/权限数）', params: '-', auth: true },
  { method: 'POST', path: '/asset/group/save', module: '用户组', desc: '新建组', params: 'Body: {groupName,description}', auth: true },
  { method: 'POST', path: '/asset/group/add-user', module: '用户组', desc: '添加用户到组', params: 'Body: {groupId,userId}', auth: true },
  { method: 'POST', path: '/asset/group/remove-user', module: '用户组', desc: '从组移除用户', params: 'Body: {groupId,userId}', auth: true },
  { method: 'POST', path: '/asset/group/set-permissions', module: '用户组', desc: '设置组权限', params: 'Body: {groupId,permissions[]}', auth: true },
  // 配件/组件
  { method: 'GET', path: '/asset/accessory/page', module: '配件管理', desc: '分页查询', params: 'Query: current,size,keyword,status,site', auth: true },
  { method: 'POST', path: '/asset/accessory/save', module: '配件管理', desc: '新增配件', params: 'Body: Accessory JSON', auth: true },
  { method: 'GET', path: '/asset/component/page', module: '组件管理', desc: '分页查询', params: 'Query: current,size,keyword,category,site', auth: true },
  { method: 'POST', path: '/asset/component/save', module: '组件管理', desc: '新增组件', params: 'Body: Component JSON', auth: true },
  // 自定义字段
  { method: 'GET', path: '/asset/custom-field/def-list', module: '自定义字段', desc: '字段定义列表', params: 'Query: site', auth: true },
  { method: 'POST', path: '/asset/custom-field/def-save', module: '自定义字段', desc: '新增字段定义', params: 'Body: {fieldName,fieldType,...}', auth: true },
  { method: 'POST', path: '/asset/custom-field/def-delete', module: '自定义字段', desc: '删除定义（级联删值）', params: 'Query: fieldId', auth: true },
  { method: 'GET', path: '/asset/custom-field/values', module: '自定义字段', desc: '获取实体字段值', params: 'Query: entityId', auth: true },
  { method: 'POST', path: '/asset/custom-field/save-values', module: '自定义字段', desc: '批量保存字段值', params: 'Body: [{entityId,fieldId,fieldValue}]', auth: true },
  // 报表
  { method: 'GET', path: '/asset/report/depreciation', module: '综合报表', desc: '折旧概览报表', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/department-summary', module: '综合报表', desc: '部门资产汇总', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/asset-lifecycle', module: '综合报表', desc: '资产全生命周期（分类×状态）', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/inventory-summary', module: '综合报表', desc: '盘点汇总报表', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/use-statistics', module: '综合报表', desc: '领用归还统计（12个月趋势）', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/consumable-comparison', module: '综合报表', desc: '耗材同比环比', params: 'Query: site', auth: true },
  { method: 'GET', path: '/asset/report/export', module: '综合报表', desc: '数据导出CSV', params: 'Query: type,format', auth: true },
  // 系统
  { method: 'GET', path: '/asset/operation-log/page', module: '系统管理', desc: '操作日志', params: 'Query: current,size', auth: true },
  { method: 'GET', path: '/asset/workflow/list', module: '系统管理', desc: '流程配置列表', params: '-', auth: true },
])

const modules = computed(() => [...new Set(apis.value.map(a => a.module))].sort())

const filteredApis = computed(() => {
  let list = apis.value
  if (filterModule.value) {
    list = list.filter(a => a.module === filterModule.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(a =>
      a.path.toLowerCase().includes(kw) ||
      a.desc.toLowerCase().includes(kw) ||
      a.module.toLowerCase().includes(kw)
    )
  }
  return list
})

const methodColor = (m) => {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[m] || 'info'
}

const onFilter = () => {
  // automatic via computed
}
</script>

<style scoped>
code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
