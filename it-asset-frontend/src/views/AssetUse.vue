<template>
  <div class="page-container">
    <!-- 顶部标题栏 -->
    <div class="header-title">资产领用管理</div>

    <!-- 搜索筛选区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="资产名称">
          <el-input
            v-model="searchForm.assetName"
            placeholder="请输入资产名称"
            clearable
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 120px;"
          >
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="逾期状态">
          <el-select
            v-model="searchForm.overdue"
            placeholder="全部"
            clearable
            style="width: 120px;"
          >
            <el-option label="正常" :value="0" />
            <el-option label="已逾期" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="operation-section">
      <el-button type="primary" @click="showApplyDialog" class="btn-apply">
        <el-icon><Plus /></el-icon> 新增
      </el-button>
      <el-button type="danger" @click="deleteSelected" :disabled="selectedIds.length === 0">
        <el-icon><Delete /></el-icon> 删除 <el-badge :value="selectedIds.length" :hidden="selectedIds.length === 0" style="margin-left:4px" />
      </el-button>
      <el-button type="warning" @click="loadPending" class="btn-pending" v-if="userRole === 2">
        <el-icon><Bell /></el-icon> 待审批
        <el-badge :value="pendingCount" :hidden="pendingCount === 0" style="margin-left: 8px;" />
      </el-button>
    </div>

    <!-- 领用记录列表 -->
    <div class="table-section">
      <el-table
        :data="useRecordList"
        v-loading="loading"
        element-loading-text="正在加载..."
        border
        stripe
        style="width: 100%;"
        @selection-change="rows => selectedIds = rows.map(r => r.recordId)"
      >
        <el-table-column type="selection" width="42" />
        <el-table-column prop="assetCode" label="资产编号" width="140" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="asset-code clickable" @click="goToAsset(row)" v-if="row.assetCode">{{ row.assetCode }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="clickable" @click="goToAsset(row)">{{ row.assetName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="领用部门" width="110" align="center" />
        <el-table-column prop="contactPerson" label="使用人" width="90" align="center" />
        <el-table-column label="领用类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.useType === 1 ? '' : 'warning'" size="small">
              {{ getUseTypeText(row.useType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="领用时间" width="110" align="center">
          <template #default="{ row }">{{ row.useDate ? formatDate(row.useDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="预期归还" width="110" align="center">
          <template #default="{ row }">{{ row.expectedReturnDate ? formatDate(row.expectedReturnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="实际归还" width="110" align="center">
          <template #default="{ row }">{{ row.actualReturnDate ? formatDate(row.actualReturnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="逾期状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.overdueStatus === 1" type="danger" size="small">已逾期</el-tag>
            <el-tag v-else-if="row.overdueStatus === 2" type="success" size="small">已关闭</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.approveStatus)]">
              {{ getStatusText(row.approveStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="90" align="center">
          <template #default="{ row }">{{ row.approveUser || '-' }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <!-- 待审批状态，管理员显示通过/拒绝 -->
            <template v-if="row.approveStatus === 0 && userRole === 2">
              <el-button type="success" size="small" @click="handleApprove(row, true)">通过</el-button>
              <el-button type="danger" size="small" @click="handleApprove(row, false)">拒绝</el-button>
            </template>
            <!-- 已通过状态且是领用类型，显示归还 -->
            <template v-else-if="row.approveStatus === 1 && (row.useType === 1 || row.useType === null)">
              <el-button type="warning" size="small" @click="handleReturn(row)">归还</el-button>
            </template>
            <!-- 其他状态显示详情 -->
            <template v-else>
              <el-button type="info" size="small" @click="showDetail(row)">详情</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && useRecordList.length === 0" description="暂无数据" />

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 领用申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="领用申请" width="600px" @close="resetApplyForm">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="90px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select
            v-model="applyForm.assetId"
            placeholder="请选择资产（仅显示未领用）"
            style="width: 100%;"
            filterable
            @change="onAssetChange"
          >
            <el-option
              v-for="asset in availableAssets"
              :key="asset.assetId"
              :label="`${asset.assetCode || ''} - ${asset.assetName}${asset.model ? ' (' + asset.model + ')' : ''}`"
              :value="asset.assetId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="领用部门" prop="department">
          <el-select v-model="applyForm.department" placeholder="请选择部门" style="width: 100%;" filterable>
            <el-option
              v-for="dept in departmentList"
              :key="dept.deptId"
              :label="dept.deptName"
              :value="dept.deptName"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="使用人" prop="contactPerson">
              <el-input v-model="applyForm.contactPerson" placeholder="请输入使用人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预期归还日期">
          <el-date-picker
            v-model="applyForm.expectedReturnDate"
            type="datetime"
            placeholder="请选择预期归还日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <!-- 选中资产后显示资产信息 -->
        <el-form-item v-if="selectedAsset" label="资产信息">
          <div class="asset-info-card">
            <div class="info-row"><span class="info-label">资产名称：</span>{{ selectedAsset.assetName }}</div>
            <div class="info-row" v-if="selectedAsset.model"><span class="info-label">型号：</span>{{ selectedAsset.model }}</div>
            <div class="info-row" v-if="selectedAsset.storageLocation"><span class="info-label">存放位置：</span>{{ selectedAsset.storageLocation }}</div>
            <div class="info-row" v-if="selectedAsset.purchasePrice"><span class="info-label">原值：</span>¥{{ Number(selectedAsset.purchasePrice).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}</div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">返回</el-button>
        <el-button type="danger" @click="submitApply">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="领用详情" width="580px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="资产编号">{{ currentRecord.assetCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentRecord.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="领用部门">{{ currentRecord.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="领用类型">{{ getUseTypeText(currentRecord.useType) }}</el-descriptions-item>
        <el-descriptions-item label="使用人">{{ currentRecord.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="领用时间">{{ currentRecord.useDate ? formatDate(currentRecord.useDate) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="预期归还">{{ currentRecord.expectedReturnDate ? formatDate(currentRecord.expectedReturnDate) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际归还">{{ currentRecord.actualReturnDate ? formatDate(currentRecord.actualReturnDate) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="归还时间">{{ currentRecord.returnDate ? formatDate(currentRecord.returnDate) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="逾期状态">
          <el-tag v-if="currentRecord.overdueStatus === 1" type="danger" size="small">已逾期</el-tag>
          <el-tag v-else-if="currentRecord.overdueStatus === 2" type="success" size="small">已关闭</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <span :class="['status-tag', getStatusClass(currentRecord.approveStatus)]">
            {{ getStatusText(currentRecord.approveStatus) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentRecord.approveUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRecord.approveTime ? formatDateTime(currentRecord.approveTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Bell } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const useRecordList = ref([])
const selectedIds = ref([])
const availableAssets = ref([])
const departmentList = ref([])
const pendingCount = ref(0)
const userRole = ref(1) // 1-普通用户 2-管理员

const searchForm = reactive({
  assetName: '',
  status: null,
  overdue: null
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const applyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

const applyForm = reactive({
  assetId: null,
  department: '',
  contactPerson: '',
  expectedReturnDate: '',
  remark: ''
})

const applyRules = reactive({
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入使用人', trigger: 'blur' }]
})

const applyFormRef = ref(null)

// 当前选中的资产信息
const selectedAsset = computed(() => {
  if (!applyForm.assetId) return null
  return availableAssets.value.find(a => a.assetId === applyForm.assetId) || null
})

onMounted(() => {
  loadList()
  loadAvailableAssets()
  loadDepartments()
  checkUserRole()
})

const checkUserRole = async () => {
  const role = localStorage.getItem('role')
  if (role) {
    userRole.value = parseInt(role)
    if (userRole.value === 2) {
      loadPending()
    }
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await request.get('/use/list-all', {
      params: {
        assetName: searchForm.assetName || undefined,
        status: searchForm.status !== null ? searchForm.status : undefined,
        overdue: searchForm.overdue !== null ? searchForm.overdue : undefined,
        current: pagination.current,
        size: pagination.size
      }
    })
    const data = res.data
    if (data && data.records) {
      useRecordList.value = data.records
      pagination.total = data.total || 0
    } else {
      useRecordList.value = data || []
      pagination.total = useRecordList.value.length
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

const loadAvailableAssets = async () => {
  try {
    // 加载所有资产，前端过滤未领用的
    const res = await request.get('/assetInfo/page', { params: { current: 1, size: 9999 } })
    const records = res.data.records || []
    availableAssets.value = records.filter(a => a.status === 0)
  } catch (error) {
    // Silently handle error
  }
}

const loadDepartments = async () => {
  try {
    const res = await request.get('/department/list')
    departmentList.value = res.data || []
  } catch (error) {
    // Silently handle error
  }
}

const loadPending = async () => {
  try {
    const res = await request.get('/use/pending')
    pendingCount.value = (res.data || []).length
  } catch (error) {
    // Silently handle error
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.assetName = ''
  searchForm.status = null
  searchForm.overdue = null
  pagination.current = 1
  loadList()
}

// 分页
const handlePageChange = () => {
  loadList()
}

// 显示申请对话框
const showApplyDialog = () => {
  loadAvailableAssets() // 刷新可用资产列表
  applyDialogVisible.value = true
}

// 重置申请表单
const resetApplyForm = () => {
  if (applyFormRef.value) {
    applyFormRef.value.resetFields()
  }
  applyForm.assetId = null
  applyForm.department = ''
  applyForm.contactPerson = ''
  applyForm.expectedReturnDate = ''
  applyForm.remark = ''
}

// 选中资产时
const onAssetChange = (assetId) => {
  const asset = availableAssets.value.find(a => a.assetId === assetId)
  if (asset && asset.storageLocation) {
    applyForm.remark = `存放位置：${asset.storageLocation}`
  }
}

// 批量删除
const deleteSelected = async () => {
  try {
    await ElMessageBox.confirm(`确认删除 ${selectedIds.value.length} 条记录？此操作不可恢复`, '删除', { type: 'warning' })
    let successCount = 0
    for (const id of selectedIds.value) {
      try {
        await request.post('/use/delete', null, { params: { recordId: id } })
        successCount++
      } catch (e) { /* 单条失败忽略 */ }
    }
    ElMessage.success(`成功删除 ${successCount} 条`)
    selectedIds.value = []
    loadList()
  } catch (e) { /* 用户取消 */ }
}

// 提交申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  try {
    await applyFormRef.value.validate()
    await request.post('/use/apply', applyForm)
    ElMessage.success('申请提交成功')
    applyDialogVisible.value = false
    loadList()
    loadPending()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('申请失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 审批
const handleApprove = async (row, approved) => {
  const action = approved ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认要${action}该领用申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await request.post('/use/approve', null, {
      params: { recordId: row.recordId, approved }
    })
    ElMessage.success(`${action}成功`)
    loadList()
    loadPending()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 归还
const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm('确认要归还该资产吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.post('/use/return', null, {
      params: { assetId: row.assetId }
    })
    ElMessage.success('归还成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('归还失败')
    }
  }
}

// 跳转到资产列表查看资产详情
const goToAsset = (row) => {
  if (row.assetCode) {
    router.push({ path: '/asset-manage', query: { code: row.assetCode } })
  }
}

// 详情
const showDetail = (row) => {
  currentRecord.value = row
  detailDialogVisible.value = true
}

// 辅助函数
const getStatusText = (status) => {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-approved', 2: 'status-rejected' }
  return map[status] || 'status-pending'
}

const getUseTypeText = (type) => {
  const map = { 1: '领用', 2: '归还', 3: '调拨' }
  return map[type] || '领用'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.clickable {
  color: #409eff;
  cursor: pointer;
}

.clickable:hover {
  text-decoration: underline;
}

/* 资产信息卡片 */
.asset-info-card {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px 16px;
  width: 100%;
}

.info-row {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

.info-label {
  color: #909399;
  display: inline-block;
  width: 70px;
}
</style>
