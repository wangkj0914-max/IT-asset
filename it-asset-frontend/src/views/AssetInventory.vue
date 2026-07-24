<template>
  <div class="asset-inventory-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="盘点状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待盘点" :value="0" />
            <el-option label="盘点中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button v-if="userRole === 2" type="success" @click="handleCreate">新建盘点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="inventoryId" label="ID" width="80" />
        <el-table-column prop="inventoryNo" label="盘点单号" width="150" />
        <el-table-column prop="inventoryName" label="盘点名称" />
        <el-table-column prop="inventoryDate" label="盘点日期" width="160" />
        <el-table-column prop="operatorName" label="盘点人" width="100" />
        <el-table-column prop="surplusCount" label="盘盈" width="80" />
        <el-table-column prop="lossCount" label="盘亏" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 0 && userRole === 2"
              size="small"
              type="warning"
              @click="handleStart(row)"
            >
              开始盘点
            </el-button>
            <el-button
              v-if="row.status === 1 && userRole === 2"
              size="small"
              type="success"
              @click="handleFinish(row)"
            >
              完成盘点
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新建盘点对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建盘点任务" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="盘点名称" prop="inventoryName">
          <el-input v-model="createForm.inventoryName" placeholder="请输入盘点名称" />
        </el-form-item>
        <el-form-item label="盘点范围" prop="inventoryRange">
          <el-select v-model="createForm.inventoryRange" placeholder="请选择范围">
            <el-option label="全部资产" :value="0" />
            <el-option label="指定资产" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="盘点详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="盘点单号">{{ currentRow.inventoryNo }}</el-descriptions-item>
        <el-descriptions-item label="盘点名称">{{ currentRow.inventoryName }}</el-descriptions-item>
        <el-descriptions-item label="盘点日期">{{ currentRow.inventoryDate }}</el-descriptions-item>
        <el-descriptions-item label="盘点人">{{ currentRow.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="盘盈数量">{{ currentRow.surplusCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="盘亏数量">{{ currentRow.lossCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentRow.status)">
            {{ getStatusText(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>盘点明细</el-divider>
      <el-table :data="detailList" border>
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="资产名称" />
        <el-table-column prop="department" label="使用部门" width="100" />
        <el-table-column prop="userName" label="使用人" width="80" />
        <el-table-column prop="status" label="盘点结果" width="100">
          <template #default="{ row }">
            <el-tag :type="getResultTag(row.status)">
              {{ getResultText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button
              v-if="currentRow.status !== 2"
              size="small"
              type="success"
              @click="handleCheck(row, 1)"
            >
              正常
            </el-button>
            <el-button
              v-if="currentRow.status !== 2"
              size="small"
              type="warning"
              @click="handleCheck(row, 2)"
            >
              盘盈
            </el-button>
            <el-button
              v-if="currentRow.status !== 2"
              size="small"
              type="danger"
              @click="handleCheck(row, 3)"
            >
              盘亏
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const detailList = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const createFormRef = ref(null)
const currentRow = ref({})

// 从 localStorage 获取当前用户角色
const userRole = computed(() => {
  const role = localStorage.getItem('role')
  return role ? parseInt(role) : 1
})

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const createForm = reactive({
  inventoryName: '',
  inventoryRange: 0,
  remark: ''
})

// 新建盘点表单验证规则
const createRules = reactive({
  inventoryName: [
    { required: true, message: '请输入盘点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '盘点名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  inventoryRange: [
    { required: true, message: '请选择盘点范围', trigger: 'change' }
  ]
})

const getStatusTag = (status) => {
  const tags = { 0: 'warning', 1: 'info', 2: 'success' }
  return tags[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待盘点', 1: '盘点中', 2: '已完成' }
  return texts[status] || '未知'
}

const getResultTag = (status) => {
  const tags = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return tags[status] || 'info'
}

const getResultText = (status) => {
  const texts = { 0: '待盘点', 1: '正常', 2: '盘盈', 3: '盘亏' }
  return texts[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/inventory/page', {
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

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleCreate = () => {
  // 重置表单
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()

    const res = await request.post('/inventory/create', createForm)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error('创建失败：' + res.msg)
    }
  } catch (error) {
    if (error.response || (error.message && error.message !== 'cancel')) {
      // 表单验证失败由 Element Plus 自动处理
      // 接口调用失败由 request.js 拦截器处理
    }
  }
}

const handleView = async (row) => {
  currentRow.value = row
  detailDialogVisible.value = true

  try {
    const res = await request.get('/inventory/details', {
      params: { inventoryId: row.inventoryId }
    })
    if (res.code === 200) {
      detailList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载明细失败')
  }
}

const handleStart = async (row) => {
  try {
    // 使用 POST 请求更新盘点任务状态
    const res = await request.post('/inventory/update', null, {
      params: { inventoryId: row.inventoryId, status: 1 }
    })
    if (res.code === 200) {
      ElMessage.success('已开始盘点')
      loadData()
    }
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
  }
}

const handleFinish = async (row) => {
  try {
    const res = await request.post('/inventory/finish', null, {
      params: { inventoryId: row.inventoryId }
    })
    if (res.code === 200) {
      ElMessage.success('盘点已完成')
      loadData()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCheck = async (row, status) => {
  try {
    const res = await request.post('/inventory/check', null, {
      params: { detailId: row.detailId, status: status }
    })
    if (res.code === 200) {
      ElMessage.success('盘点完成')
      handleView(currentRow.value)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.asset-inventory-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.table-card {
  min-height: 500px;
}
</style>
