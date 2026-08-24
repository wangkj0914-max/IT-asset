<template>
  <div class="page-container">
    <div class="header-title"><span>资产归还</span></div>
    
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.approveStatus" placeholder="全部" clearable>
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="资产编号/名称" clearable style="width:180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="showSubmit">提交归还申请</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-section">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="returnPerson" label="归还人" width="100" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column label="站点" width="90">
          <template #default="{ row }">{{ row.site || '-' }}</template>
        </el-table-column>
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">{{ row.returnDate ? formatDate(row.returnDate) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.approveStatus)">{{ statusLabel(row.approveStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnReason" label="归还说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="approveUser" label="审批人" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.approveStatus === 0 && userRole === 2" size="small" type="success" @click="approve(row, true)">通过</el-button>
            <el-button v-if="row.approveStatus === 0 && userRole === 2" size="small" type="danger" @click="approve(row, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无资产归还记录" />
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData" @current-change="loadData"
        />
      </div>
    </div>

    <!-- 提交归还申请 -->
    <el-dialog v-model="submitVisible" title="提交归还申请" width="520px">
      <el-form :model="submitForm" label-width="100px">
        <el-form-item label="资产名称">
          <el-select
            v-model="submitForm.assetId"
            filterable
            remote
            :remote-method="searchAssets"
            :loading="assetSearching"
            placeholder="输入关键字搜索资产名称/编号"
            style="width:100%"
            @change="onAssetSelect"
          >
            <el-option
              v-for="a in assetOptions"
              :key="a.assetId"
              :label="`${a.assetName} (${a.assetCode||'无编号'})`"
              :value="a.assetId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="归还人"><el-input v-model="submitForm.returnPerson" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="submitForm.department" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="submitForm.conditionStatus">
            <el-option label="完好" :value="0" />
            <el-option label="损坏" :value="1" />
            <el-option label="需维修" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="归还说明"><el-input v-model="submitForm.returnReason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReturn">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="归还详情" width="520px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编号">{{ currentRecord.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentRecord.assetName }}</el-descriptions-item>
        <el-descriptions-item label="归还人">{{ currentRecord.returnPerson }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentRecord.department }}</el-descriptions-item>
        <el-descriptions-item label="归还时间">{{ currentRecord.returnDate ? formatDate(currentRecord.returnDate) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(currentRecord.approveStatus)">{{ statusLabel(currentRecord.approveStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产状况">
          {{ ['完好','损坏','需维修'][currentRecord.conditionStatus] }}
        </el-descriptions-item>
        <el-descriptions-item label="站点">{{ currentRecord.site || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录ID">{{ currentRecord.returnId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRecord.createTime ? formatDate(currentRecord.createTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="归还说明" :span="2">{{ currentRecord.returnReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentRecord.approveUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRecord.approveTime ? formatDate(currentRecord.approveTime) : '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role')||'1'))
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ approveStatus: null, keyword: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const submitVisible = ref(false)
const detailVisible = ref(false)
const currentRecord = ref({})
const assetOptions = ref([])
const assetSearching = ref(false)
const submitForm = reactive({
  assetId: null, returnPerson: localStorage.getItem('username')||'',
  department: '', conditionStatus: 0, returnReason: ''
})

const formatDate = (s) => {
  if (!s) return '-'
  const d = new Date(s)
  return d.toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}
const statusLabel = (s) => ['待审批','已通过','已拒绝'][s] || '未知'
const statusTagType = (s) => s === 1 ? 'success' : s === 2 ? 'danger' : 'warning'

const loadData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (searchForm.approveStatus !== null && searchForm.approveStatus !== '') params.approveStatus = searchForm.approveStatus
    if (searchForm.keyword) params.keyword = searchForm.keyword
    const res = await request.get('/return/page', { params })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) { ElMessage.error('加载失败') }
  loading.value = false
}

const handleSearch = () => { pagination.current = 1; loadData() }
const showSubmit = () => { Object.assign(submitForm, { assetId: null, returnPerson: localStorage.getItem('username')||'', department:'', conditionStatus:0, returnReason:'' }); assetOptions.value=[]; submitVisible.value = true }

const searchAssets = async (kw) => {
  if (!kw) { assetOptions.value = []; return }
  assetSearching.value = true
  try {
    const r = await request.get('/assetInfo/page', { params: { keyword: kw, current: 1, size: 20, status: 1 } })
    if (r.code === 200) assetOptions.value = r.data.records || []
  } catch (e) { /* 搜索失败静默忽略 */ }
  assetSearching.value = false
}

const onAssetSelect = (val) => {
  const found = assetOptions.value.find(a => a.assetId === val)
  if (found) {
    // 根据固定资产自动带出归还人与部门
    // 归还人:优先取资产使用人(userName),兜底责任人(responsiblePerson)或当前登录用户
    submitForm.returnPerson = found.userName || found.responsiblePerson || localStorage.getItem('username') || ''
    // 部门:取资产使用部门
    submitForm.department = found.department || ''
  }
}

const submitReturn = async () => {
  try {
    const r = await request.post('/return/submit', { ...submitForm, assetId: Number(submitForm.assetId) })
    if (r.code === 200) { ElMessage.success('提交成功'); submitVisible.value = false; loadData() }
    else ElMessage.error(r.msg)
  } catch (e) { ElMessage.error('提交失败') }
}
const showDetail = (row) => { currentRecord.value = row; detailVisible.value = true }
const approve = async (row, ok) => {
  try {
    await ElMessageBox.confirm(`确认${ok?'通过':'拒绝'}该归还申请？`, '审批', { type: 'warning' })
    const r = await request.post('/return/approve', null, { params: { returnId: row.returnId, approved: ok } })
    if (r.code === 200) { ElMessage.success('已' + (ok?'通过':'拒绝')); loadData() }
    else ElMessage.error(r.msg)
  } catch (e) { /* 用户取消操作 */ }
}

onMounted(loadData)
</script>

<style scoped>
</style>
