<template>
  <div class="page-container">
    <div class="header-title">
      <span>资产入库管理</span>
      <el-button type="primary" @click="showInboundDialog">
        <el-icon><Plus /></el-icon> 新增入库
      </el-button>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="资产名称">
          <el-input v-model="searchForm.assetName" placeholder="请输入资产名称" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.inboundNo" placeholder="请输入入库单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px;">
            <el-option label="待审核" :value="0" />
            <el-option label="已入库" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 入库记录列表 -->
    <div class="table-section">
      <el-table :data="inboundList" v-loading="loading" stripe>
        <el-table-column prop="inboundNo" label="入库单号" width="150" align="center" />
        <el-table-column prop="assetName" label="资产名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="brand" label="品牌" width="100" align="center" />
        <el-table-column prop="model" label="型号" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="purchasePrice" label="采购价格" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.purchasePrice">¥{{ row.purchasePrice.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="150" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态，管理员显示审核按钮 -->
            <template v-if="row.status === 0 && userRole === 2">
              <el-button type="success" size="small" @click="handleAudit(row, true)">审核</el-button>
              <el-button type="danger" size="small" @click="handleAudit(row, false)">拒绝</el-button>
            </template>
            <!-- 其他状态显示详情 -->
            <template v-else>
              <el-button type="info" size="small" @click="showDetail(row)">详情</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && inboundList.length === 0" description="暂无数据" />

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 新增入库对话框 -->
    <el-dialog v-model="inboundDialogVisible" title="资产入库申请" width="650px">
      <el-form :model="inboundForm" :rules="inboundRules" ref="inboundFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="inboundForm.assetName" placeholder="请输入资产名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产分类" prop="categoryId">
              <el-select v-model="inboundForm.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option v-for="cat in categoryList" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="inboundForm.brand" placeholder="请输入品牌" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="inboundForm.model" placeholder="请输入型号" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input v-model="inboundForm.serialNumber" placeholder="请输入序列号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价格" prop="purchasePrice">
              <el-input v-model="inboundForm.purchasePrice" placeholder="请输入采购价格" type="number" step="0.01" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input v-model="inboundForm.supplier" placeholder="请输入供应商" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="storageLocation">
              <el-input v-model="inboundForm.storageLocation" placeholder="请输入存放位置" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="inboundForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="inboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="入库详情" width="600px">
      <el-descriptions :column="1" border v-if="currentInbound">
        <el-descriptions-item label="入库单号">{{ currentInbound.inboundNo }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentInbound.assetName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentInbound.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ currentInbound.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentInbound.model }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ currentInbound.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="采购价格">
          <span v-if="currentInbound.purchasePrice">¥{{ currentInbound.purchasePrice.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentInbound.supplier }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ currentInbound.storageLocation }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <span :class="['status-tag', getStatusClass(currentInbound.status)]">
            {{ getStatusText(currentInbound.status) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentInbound.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentInbound.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentInbound.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const inboundList = ref([])
const categoryList = ref([])
const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))

const searchForm = reactive({
  assetName: '',
  inboundNo: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const inboundDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentInbound = ref(null)
const inboundFormRef = ref(null)

const inboundForm = reactive({
  assetName: '',
  categoryId: null,
  brand: '',
  model: '',
  serialNumber: '',
  purchasePrice: '',
  supplier: '',
  storageLocation: '',
  remark: ''
})

const inboundRules = reactive({
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  serialNumber: [{ required: true, message: '请输入序列号', trigger: 'blur' }],
  storageLocation: [{ required: true, message: '请输入存放位置', trigger: 'blur' }]
})

onMounted(() => {
  loadCategories()
  loadInboundList()
})

const loadCategories = async () => {
  try {
    const res = await request.get('/category/list')
    categoryList.value = res.data || []
  } catch (error) {
    // Silently handle error
  }
}

const loadInboundList = async () => {
  loading.value = true
  try {
    const res = await request.get('/inbound/list-all', {
      params: {
        current: pagination.current,
        size: pagination.size,
        assetName: searchForm.assetName,
        inboundNo: searchForm.inboundNo,
        status: searchForm.status
      }
    })
    inboundList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载入库列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadInboundList()
}

const resetSearch = () => {
  searchForm.assetName = ''
  searchForm.inboundNo = ''
  searchForm.status = null
  pagination.current = 1
  loadInboundList()
}

const showInboundDialog = () => {
  Object.assign(inboundForm, {
    assetName: '',
    categoryId: null,
    brand: '',
    model: '',
    serialNumber: '',
    purchasePrice: '',
    supplier: '',
    storageLocation: '',
    remark: ''
  })
  inboundDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!inboundFormRef.value) return
  try {
    await inboundFormRef.value.validate()
    
    const submitData = {
      ...inboundForm,
      purchasePrice: inboundForm.purchasePrice ? Number(inboundForm.purchasePrice) : null
    }
    
    await request.post('/inbound/apply', submitData)
    ElMessage.success('入库申请提交成功，等待审核')
    inboundDialogVisible.value = false
    loadInboundList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

const handleAudit = async (row, approved) => {
  const action = approved ? '通过' : '拒绝'
  ElMessageBox.confirm(`确认要${action}该入库申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await request.post('/inbound/audit', null, {
        params: { inboundId: row.inboundId, approved }
      })
      ElMessage.success(`${action}成功`)
      loadInboundList()
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

const showDetail = (row) => {
  currentInbound.value = row
  detailDialogVisible.value = true
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已入库', 2: '已拒绝' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-pending', 1: 'status-completed', 2: 'status-rejected' }
  return map[status] || 'status-pending'
}

const handleSizeChange = () => loadInboundList()
const handleCurrentChange = () => loadInboundList()
</script>

<style scoped>
.page-container {
  width: 95%;
  margin: 0 auto;
  padding: 20px;
}

.header-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.search-section {
  background: white;
  padding: 20px 20px 10px;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.table-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}
</style>
