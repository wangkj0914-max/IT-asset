<template>
  <div class="page-container">
    <div class="header-title"><span>资产报废</span></div>
    
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="资产名称">
          <el-input v-model="searchForm.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="报废类型">
          <el-select v-model="searchForm.scrapType" placeholder="请选择类型" clearable>
            <el-option label="正常报废" :value="0" />
            <el-option label="损坏报废" :value="1" />
            <el-option label="丢失报废" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="searchForm.approveStatus" placeholder="请选择状态" clearable>
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="danger" @click="handleApply">申请报废</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="scrapId" label="报废 ID" width="80" />
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="scrapReason" label="报废原因" show-overflow-tooltip />
        <el-table-column prop="scrapType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.scrapType)">
              {{ getTypeText(row.scrapType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原值" width="100">
          <template #default="{ row }">¥{{ row.originalPrice }}</template>
        </el-table-column>
        <el-table-column prop="residualValue" label="残值" width="100">
          <template #default="{ row }">¥{{ row.residualValue || 0 }}</template>
        </el-table-column>
        <el-table-column prop="approveStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.approveStatus)">
              {{ getStatusText(row.approveStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.approveStatus === 0 && userRole === 2"
              size="small"
              type="success"
              @click="handleApprove(row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.approveStatus === 0 && userRole === 2"
              size="small"
              type="danger"
              @click="handleApprove(row, 2)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 申请报废对话框 -->
    <el-dialog v-model="applyDialogVisible" title="申请报废" width="600px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select
            v-model="applyForm.assetId"
            placeholder="请选择要报废的资产"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in assetList"
              :key="item.assetId"
              :label="`${item.assetCode} - ${item.assetName}`"
              :value="item.assetId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报废类型" prop="scrapType">
          <el-select v-model="applyForm.scrapType" placeholder="请选择报废类型" style="width: 100%">
            <el-option label="正常报废" :value="0" />
            <el-option label="损坏报废" :value="1" />
            <el-option label="丢失报废" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="报废原因" prop="scrapReason">
          <el-input
            v-model="applyForm.scrapReason"
            type="textarea"
            :rows="3"
            placeholder="请详细说明报废原因"
          />
        </el-form-item>
        <el-form-item label="资产原值">
          <el-input-number v-model="applyForm.originalPrice" :precision="2" :step="100" :min="0" />
        </el-form-item>
        <el-form-item label="残值">
          <el-input-number v-model="applyForm.residualValue" :precision="2" :step="10" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="报废详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="资产名称">{{ currentRow.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentRow.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="报废原因">{{ currentRow.scrapReason }}</el-descriptions-item>
        <el-descriptions-item label="报废类型">
          <el-tag :type="getTypeTag(currentRow.scrapType)">
            {{ getTypeText(currentRow.scrapType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产原值">¥{{ currentRow.originalPrice }}</el-descriptions-item>
        <el-descriptions-item label="残值">¥{{ currentRow.residualValue || 0 }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="申请部门">{{ currentRow.applyDepartment }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusTag(currentRow.approveStatus)">
            {{ getStatusText(currentRow.approveStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批人" v-if="currentRow.approveUser">{{ currentRow.approveUser }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const applyDialogVisible = ref(false)
const applyFormRef = ref(null)
const currentRow = ref({})
const assetList = ref([])

// 从 localStorage 获取当前用户角色
const userRole = computed(() => {
  const role = localStorage.getItem('role')
  return role ? parseInt(role) : 1
})

const searchForm = reactive({
  assetName: '',
  scrapType: null,
  approveStatus: null
})

const applyForm = reactive({
  assetId: null,
  scrapType: 0,
  scrapReason: '',
  originalPrice: 0,
  residualValue: 0,
  remark: ''
})

// 报废申请表单验证规则
const applyRules = reactive({
  assetId: [
    { required: true, message: '请选择要报废的资产', trigger: 'change' }
  ],
  scrapType: [
    { required: true, message: '请选择报废类型', trigger: 'change' }
  ],
  scrapReason: [
    { required: true, message: '请填写报废原因', trigger: 'blur' },
    { min: 5, message: '报废原因不能少于 5 个字符', trigger: 'blur' }
  ]
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getTypeTag = (type) => {
  const tags = { 0: 'info', 1: 'warning', 2: 'danger' }
  return tags[type] || 'info'
}

const getTypeText = (type) => {
  const texts = { 0: '正常报废', 1: '损坏报废', 2: '丢失报废' }
  return texts[type] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'warning', 1: 'success', 2: 'danger' }
  return tags[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/scrap/page', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        scrapType: searchForm.scrapType,
        approveStatus: searchForm.approveStatus
      }
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.assetName = ''
  searchForm.scrapType = null
  searchForm.approveStatus = null
  handleSearch()
}

const handleView = (row) => {
  currentRow.value = row
  dialogVisible.value = true
}

// 加载可用资产列表（未报废的资产）
const loadAssets = async () => {
  try {
    const res = await request.get('/assetInfo/list', {
      params: { status: 1 }  // 只查询在用资产
    })
    if (res.code === 200) {
      assetList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载资产列表失败')
  }
}

// 打开报废申请对话框
const handleApply = async () => {
  await loadAssets()
  if (assetList.value.length === 0) {
    ElMessage.warning('没有可报废的资产')
    return
  }
  // 使用 formRef.resetFields() 重置表单
  if (applyFormRef.value) {
    applyFormRef.value.resetFields()
  }
  applyDialogVisible.value = true
}

// 提交报废申请
const submitApply = async () => {
  if (!applyFormRef.value) return

  try {
    await applyFormRef.value.validate()

    const res = await request.post('/scrap/apply', applyForm)
    if (res.code === 200) {
      ElMessage.success('报废申请提交成功')
      applyDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    if (error.response || (error.message && error.message !== 'cancel')) {
      // 表单验证失败，Element Plus 会自动提示，不重复弹窗
      // 接口调用失败由 request.js 拦截器处理
    }
  }
}

const handleApprove = async (row, status) => {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 1 ? '通过' : '拒绝'}该报废申请吗？`,
      '确认操作',
      { type: 'warning' }
    )

    // 后端接口需要 approved (Boolean) 参数
    const res = await request.post('/scrap/approve', null, {
      params: {
        scrapId: row.scrapId,
        approved: status === 1  // true=通过，false=拒绝
      }
    })

    if (res.code === 200) {
      ElMessage.success('操作成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

onMounted(() => {
  loadData()
})
</script>

<style scoped>
</style>
