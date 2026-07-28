<template>
  <div class="page-container">
    <!-- 标题 -->
    <div class="header-title"><span>资产维修</span></div>
    
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="资产名称">
          <el-input v-model="searchForm.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="维修状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待维修" :value="0" />
            <el-option label="维修中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-section">
      <el-button type="primary" @click="showApplyDialog">
        <el-icon><Plus /></el-icon> 报修申请
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="repairId" label="维修 ID" width="80" />
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="repairReason" label="维修原因" show-overflow-tooltip />
        <el-table-column prop="applyUserName" label="报修人" width="100" />
        <el-table-column prop="applyDepartment" label="部门" width="120" />
        <el-table-column prop="repairCost" label="维修费用" width="100">
          <template #default="{ row }">
            <span v-if="row.repairCost">¥{{ row.repairCost }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="repairStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.repairStatus)">
              {{ getStatusText(row.repairStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="row.repairStatus === 0" 
              size="small" 
              type="warning"
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button 
              v-if="row.repairStatus === 1" 
              size="small" 
              type="success"
              @click="handleComplete(row)"
            >
              完成
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

    <!-- 报修申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="报修申请" width="500px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="80px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select v-model="applyForm.assetId" placeholder="请选择要报修的资产" filterable style="width: 100%;">
            <el-option 
              v-for="asset in availableAssets" 
              :key="asset.assetId" 
              :label="`${asset.assetName} (${asset.assetCode})`" 
              :value="asset.assetId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维修原因" prop="repairReason">
          <el-input v-model="applyForm.repairReason" type="textarea" :rows="3" placeholder="请描述故障情况" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" title="维修详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="资产名称">{{ currentRow.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentRow.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="维修原因">{{ currentRow.repairReason }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentRow.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="报修部门">{{ currentRow.applyDepartment }}</el-descriptions-item>
        <el-descriptions-item label="维修状态">
          <el-tag :type="getStatusType(currentRow.repairStatus)">
            {{ getStatusText(currentRow.repairStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维修费用" v-if="currentRow.repairCost">
          ¥{{ currentRow.repairCost }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理维修" width="500px">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="维修人员">
          <el-input v-model="processForm.repairMan" placeholder="请输入维修人员" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="processForm.status" placeholder="请选择状态">
            <el-option label="维修中" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成维修" width="500px">
      <el-form :model="completeForm" label-width="80px">
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.cost" :min="0" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.remark" type="textarea" :rows="3" placeholder="请输入维修备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const processDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const applyDialogVisible = ref(false)
const currentRow = ref({})
const availableAssets = ref([])
const applyFormRef = ref(null)

const searchForm = reactive({
  assetName: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const applyForm = reactive({
  assetId: null,
  repairReason: '',
  applyUserId: null,
  applyUserName: '',
  applyDepartment: '',
  remark: ''
})

const applyRules = reactive({
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  repairReason: [{ required: true, message: '请填写维修原因', trigger: 'blur' }]
})

const processForm = reactive({
  repairId: null,
  repairMan: '',
  status: 1
})

const completeForm = reactive({
  repairId: null,
  cost: 0,
  remark: ''
})

// 获取状态类型
const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = { 0: '待维修', 1: '维修中', 2: '已完成' }
  return texts[status] || '未知'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/page', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        status: searchForm.status
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

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.assetName = ''
  searchForm.status = null
  handleSearch()
}

// 显示报修申请对话框
const showApplyDialog = async () => {
  // 从 localStorage 获取当前用户信息
  applyForm.applyUserId = localStorage.getItem('userId') || null
  applyForm.applyUserName = localStorage.getItem('realName') || localStorage.getItem('username') || ''
  applyForm.applyDepartment = localStorage.getItem('department') || ''
  
  // 加载可用资产（状态不是维修中和已报废的）
  try {
    const res = await request.get('/assetInfo/page', {
      params: { current: 1, size: 1000 }
    })
    availableAssets.value = (res.data.records || []).filter(a => a.status !== 2 && a.status !== 3)
  } catch (e) {
    ElMessage.error('加载资产列表失败')
    return
  }
  
  applyDialogVisible.value = true
}

// 提交报修申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  try {
    await applyFormRef.value.validate()
    const res = await request.post('/repair/apply', {
      assetId: applyForm.assetId,
      repairReason: applyForm.repairReason,
      applyUserId: applyForm.applyUserId,
      applyUserName: applyForm.applyUserName,
      applyDepartment: applyForm.applyDepartment,
      remark: applyForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('报修申请提交成功')
      applyDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  dialogVisible.value = true
}

// 处理维修
const handleProcess = (row) => {
  currentRow.value = row
  processForm.repairId = row.repairId
  processDialogVisible.value = true
}

// 提交处理
const submitProcess = async () => {
  try {
    const res = await request.post('/repair/update-status', null, {
      params: {
        repairId: processForm.repairId,
        status: processForm.status,
        repairMan: processForm.repairMan
      }
    })
    if (res.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

// 完成维修
const handleComplete = (row) => {
  currentRow.value = row
  completeForm.repairId = row.repairId
  completeDialogVisible.value = true
}

// 提交完成
const submitComplete = async () => {
  try {
    const res = await request.post('/repair/complete', null, {
      params: {
        repairId: completeForm.repairId,
        cost: completeForm.cost,
        remark: completeForm.remark
      }
    })
    if (res.code === 200) {
      ElMessage.success('维修完成')
      completeDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    ElMessage.error('完成失败')
  }
}

// 分页
const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

onMounted(() => {
  loadData()
})
</script>

<style scoped>
</style>
