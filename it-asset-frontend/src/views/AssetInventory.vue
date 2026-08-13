<template>
  <div class="page-container">
    <!-- 标题 -->
    <div class="header-title"><span>资产盘点</span></div>
    
    <!-- 搜索栏 -->
    <div class="search-section">
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
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
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
            <el-button
              v-if="row.status === 2"
              size="small"
              type="info"
              @click="handleReport(row)"
            >
              查看报告
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
    </div>

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
    <el-dialog v-model="detailDialogVisible" title="盘点详情" width="900px">
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

      <div class="detail-toolbar">
        <span class="detail-title">盘点明细</span>
        <div class="toolbar-actions" v-if="currentRow.status !== 2">
          <el-button type="success" size="small" @click="quickScanOpen = true">
            <el-icon><Camera /></el-icon> 扫码盘点
          </el-button>
          <el-button size="small" @click="scanAllPending">
            <el-icon><Position /></el-icon> 一键全盘
          </el-button>
        </div>
      </div>
      <el-table :data="detailList" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="assetCode" label="资产编号" width="130" fixed="left" />
        <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="department" label="使用部门" width="100" show-overflow-tooltip />
        <el-table-column prop="userName" label="使用人" width="80" />
        <el-table-column prop="actualLocation" label="实际位置" width="120" show-overflow-tooltip />
        <el-table-column prop="differenceType" label="差异类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.differenceType" :type="getDiffTypeTag(row.differenceType)" size="small">
              {{ getDiffTypeText(row.differenceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scannedAt" label="扫描时间" width="160" />
        <el-table-column prop="status" label="盘点结果" width="100" fixed="right">
          <template #default="{ row }">
            <el-tag :type="getResultTag(row.status)">
              {{ getResultText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="currentRow.status !== 2"
              size="small"
              type="primary"
              @click="openCheckDialog(row)"
            >
              盘点
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 盘点操作对话框 -->
    <el-dialog v-model="checkDialogVisible" title="盘点操作" width="450px">
      <el-form :model="checkForm" label-width="90px">
        <el-form-item label="资产编号">
          <el-input :model-value="checkForm.assetCode" disabled />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input :model-value="checkForm.assetName" disabled />
        </el-form-item>
        <el-form-item label="盘点结果" prop="status">
          <el-select v-model="checkForm.status" placeholder="请选择">
            <el-option label="正常" :value="1" />
            <el-option label="盘盈" :value="2" />
            <el-option label="盘亏" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际位置">
          <el-input v-model="checkForm.actualLocation" placeholder="请输入实际盘点位置" />
        </el-form-item>
        <el-form-item label="差异类型">
          <el-select v-model="checkForm.differenceType" placeholder="请选择差异类型" clearable>
            <el-option label="位置不符" value="LOCATION" />
            <el-option label="状态不符" value="STATUS" />
            <el-option label="缺失" value="MISSING" />
            <el-option label="盘盈" value="EXTRA" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">确定</el-button>
      </template>
    </el-dialog>

    <!-- 快速扫码盘点 -->
    <el-dialog v-model="quickScanOpen" title="扫码盘点" width="480px" @closed="onQuickScanClose">
      <p class="scan-tip">扫描或输入资产编号，自动定位并标记为正常</p>
      <input ref="quickScanFile" type="file" accept="image/*" capture="environment" style="display:none" @change="handleQuickScanFile" />
      <div class="quick-scan-actions">
        <el-button type="primary" @click="$refs.quickScanFile.click()" :loading="scanLoading">
          <el-icon><Camera /></el-icon> 拍照扫码
        </el-button>
        <span class="divider">或</span>
        <el-input v-model="quickScanCode" placeholder="输入资产编号" @keyup.enter="doQuickScan" clearable style="flex:1">
          <template #append>
            <el-button @click="doQuickScan">确认</el-button>
          </template>
        </el-input>
      </div>
      <div v-if="quickScanLog.length > 0" class="scan-log">
        <el-divider>本次已盘点 ({{ quickScanLog.length }})</el-divider>
        <div v-for="item in quickScanLog" :key="item.assetCode" class="log-item">
          <el-tag size="small" :type="item.success ? 'success' : 'danger'">{{ item.success ? '✓' : '✗' }}</el-tag>
          <span style="margin-left:8px">{{ item.assetCode }} {{ item.assetName }}</span>
          <span style="margin-left:auto;color:#94A3B8;font-size:12px">{{ item.msg }}</span>
        </div>
        <el-button size="small" type="text" @click="quickScanLog = []">清空记录</el-button>
      </div>
      <template #footer>
        <el-button @click="quickScanOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 盘点报告对话框 -->
    <el-dialog v-model="reportDialogVisible" title="盘点差异报告" width="800px">
      <div class="report-summary">
        <el-row :gutter="12">
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-normal">
              <div class="summary-value">{{ reportData.normalCount || 0 }}</div>
              <div class="summary-label">正常</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-surplus">
              <div class="summary-value">{{ reportData.surplusCount || 0 }}</div>
              <div class="summary-label">盘盈</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-loss">
              <div class="summary-value">{{ reportData.lossCount || 0 }}</div>
              <div class="summary-label">盘亏</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-diff">
              <div class="summary-value">{{ (reportData.locationDiffCount || 0) + (reportData.statusDiffCount || 0) }}</div>
              <div class="summary-label">位置/状态不符</div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="12" style="margin-top: 12px;">
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-missing">
              <div class="summary-value">{{ reportData.missingCount || 0 }}</div>
              <div class="summary-label">缺失</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="summary-card summary-extra">
              <div class="summary-value">{{ reportData.extraCount || 0 }}</div>
              <div class="summary-label">盘盈(额外)</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-divider>差异明细</el-divider>
      <el-table :data="reportData.details || []" border max-height="350">
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="资产名称" min-width="120" />
        <el-table-column prop="storageLocation" label="原位置" width="120" />
        <el-table-column prop="actualLocation" label="实际位置" width="120" />
        <el-table-column prop="status" label="盘点结果" width="90">
          <template #default="{ row }">
            <el-tag :type="getResultTag(row.status)" size="small">
              {{ getResultText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="differenceType" label="差异类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.differenceType" :type="getDiffTypeTag(row.differenceType)" size="small">
              {{ getDiffTypeText(row.differenceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultRemark" label="说明" min-width="80" />
      </el-table>
      <el-empty v-if="!reportData.details || reportData.details.length === 0" description="未发现差异项" />

      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Position } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const detailList = ref([])
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const quickScanOpen = ref(false)
const quickScanCode = ref('')
const quickScanFile = ref(null)
const scanLoading = ref(false)
const quickScanLog = ref([])
const checkDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const createFormRef = ref(null)
const currentRow = ref({})
const reportData = ref({})

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

const checkForm = reactive({
  detailId: null,
  assetCode: '',
  assetName: '',
  status: 1,
  actualLocation: '',
  differenceType: '',
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

const getDiffTypeTag = (type) => {
  const tags = { LOCATION: 'warning', STATUS: 'info', MISSING: 'danger', EXTRA: 'success' }
  return tags[type] || 'info'
}

const getDiffTypeText = (type) => {
  const texts = { LOCATION: '位置不符', STATUS: '状态不符', MISSING: '缺失', EXTRA: '盘盈' }
  return texts[type] || '未知'
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

const openCheckDialog = (row) => {
  checkForm.detailId = row.detailId
  checkForm.assetCode = row.assetCode
  checkForm.assetName = row.assetName
  checkForm.status = row.status || 1
  checkForm.actualLocation = row.actualLocation || ''
  checkForm.differenceType = row.differenceType || ''
  checkForm.remark = row.resultRemark || ''
  checkDialogVisible.value = true
}

const submitCheck = async () => {
  try {
    const res = await request.post('/inventory/check', null, {
      params: {
        detailId: checkForm.detailId,
        status: checkForm.status,
        remark: checkForm.remark,
        actualLocation: checkForm.actualLocation,
        differenceType: checkForm.differenceType
      }
    })
    if (res.code === 200) {
      ElMessage.success('盘点完成')
      checkDialogVisible.value = false
      handleView(currentRow.value)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleReport = async (row) => {
  currentRow.value = row
  try {
    const res = await request.get('/inventory/report', {
      params: { inventoryId: row.inventoryId }
    })
    if (res.code === 200) {
      reportData.value = res.data
      reportDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取报告失败')
  }
}

const handleSizeChange = () => loadData()
const handlePageChange = () => loadData()

// 处理拍照扫码图片
const handleQuickScanFile = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  scanLoading.value = true
  try {
    const scanner = new Html5Qrcode('quick-scan-temp')
    const result = await scanner.scanFile(file, false)
    scanner.clear()
    quickScanCode.value = result
    await doQuickScan()
  } catch {
    ElMessage.error('未能识别二维码，请重试或手动输入')
  }
  scanLoading.value = false
  if (quickScanFile.value) quickScanFile.value.value = ''
}

// 扫码盘点：定位并标记为正常
const doQuickScan = async () => {
  const code = quickScanCode.value.trim()
  if (!code) { ElMessage.warning('请输入资产编号'); return }
  const detail = detailList.value.find(d => d.assetCode === code)
  if (!detail) {
    quickScanLog.value.unshift({ assetCode: code, assetName: '?', success: false, msg: '不在本盘点单中' })
    ElMessage.error(`资产 ${code} 不在本次盘点明细中`)
    quickScanCode.value = ''
    return
  }
  if (detail.status === 1) {
    quickScanLog.value.unshift({ assetCode: code, assetName: detail.assetName, success: false, msg: '已盘点' })
    ElMessage.warning(`资产 ${code} 已盘点，请勿重复`)
    quickScanCode.value = ''
    return
  }
  try {
    await request.post('/inventory/check', null, {
      params: { detailId: detail.detailId, status: 1, remark: '扫码盘点' }
    })
    detail.status = 1
    detail.scannedAt = new Date().toISOString().replace('T', ' ').slice(0, 19)
    quickScanLog.value.unshift({ assetCode: code, assetName: detail.assetName, success: true, msg: '已标记正常' })
    ElMessage.success(`${code} 盘点成功`)
    quickScanCode.value = ''
  } catch (e) {
    quickScanLog.value.unshift({ assetCode: code, assetName: detail.assetName, success: false, msg: '提交失败' })
    ElMessage.error('盘点提交失败')
  }
}

const onQuickScanClose = () => {
  quickScanLog.value = []
  quickScanCode.value = ''
}

// 一键全部标记为正常
const scanAllPending = async () => {
  const pending = detailList.value.filter(d => d.status !== 1)
  if (pending.length === 0) { ElMessage.info('没有待盘点的资产'); return }
  try {
    await ElMessage.confirm(`确认将 ${pending.length} 项未盘点资产全部标记为正常？`, '提示', { type: 'warning' })
  } catch { return }
  let count = 0
  for (const d of pending) {
    try {
      await request.post('/inventory/check', null, {
        params: { detailId: d.detailId, status: 1, remark: '一键全盘' }
      })
      d.status = 1
      d.scannedAt = new Date().toISOString().replace('T', ' ').slice(0, 19)
      count++
    } catch { /* 单条失败继续下一条 */ }
  }
  ElMessage.success(`已完成 ${count} 项盘点`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E2E8F0;
}
.detail-title {
  font-size: 15px;
  font-weight: bold;
  color: #1A1A2E;
}
.toolbar-actions { display: flex; gap: 8px; }
.quick-scan-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0;
}
.divider { color: #94A3B8; font-size: 13px; }
.scan-tip {
  color: #64748B;
  font-size: 13px;
  margin: 0 0 12px;
}
.scan-log {
  max-height: 240px;
  overflow-y: auto;
  margin-top: 16px;
}
.log-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #F1F5F9;
  font-size: 13px;
}
.report-summary {
  margin-bottom: 10px;
}
.summary-card {
  text-align: center;
  cursor: default;
}
.summary-card .summary-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}
.summary-card .summary-label {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}
.summary-normal .summary-value { color: #67c23a; }
.summary-surplus .summary-value { color: #e6a23c; }
.summary-loss .summary-value { color: #f56c6c; }
.summary-diff .summary-value { color: #909399; }
.summary-missing .summary-value { color: #f56c6c; }
.summary-extra .summary-value { color: #67c23a; }
</style>
