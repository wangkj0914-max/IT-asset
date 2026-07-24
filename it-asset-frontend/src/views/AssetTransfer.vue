<template>
  <div class="transfer-container">
    <!-- 搜索 -->
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable @change="loadData">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="showApply">新建调拨</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="transferNo" label="调拨单号" width="160" />
        <el-table-column prop="assetName" label="资产名称" min-width="140" />
        <el-table-column prop="assetCode" label="资产编号" width="120" />
        <el-table-column prop="fromDepartment" label="调出部门" width="100" />
        <el-table-column prop="toDepartment" label="调入部门" width="100" />
        <el-table-column prop="transferReason" label="调拨原因" min-width="140" show-overflow-tooltip />
        <el-table-column prop="transferStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.transferStatus)">{{ statusText(row.transferStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button
              v-if="row.transferStatus === 0 && userRole === 2"
              size="small" type="success" @click="handleApprove(row, 1)"
            >通过</el-button>
            <el-button
              v-if="row.transferStatus === 0 && userRole === 2"
              size="small" type="danger" @click="handleApprove(row, 2)"
            >拒绝</el-button>
            <el-button
              v-if="row.transferStatus === 2 && userRole === 2"
              size="small" type="danger" @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无调拨记录" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新建对话框 -->
    <el-dialog v-model="applyVisible" title="新建资产调拨" width="500px" @closed="resetApplyForm">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
        <el-form-item label="选择资产" prop="assetId">
          <el-select v-model="applyForm.assetId" filterable placeholder="搜索并选择资产" style="width:100%">
            <el-option v-for="a in assetList" :key="a.assetId" :label="a.assetName + ' (' + (a.assetCode||'') + ')'" :value="a.assetId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入部门" prop="toDepartment">
          <el-select v-model="applyForm.toDepartment" filterable placeholder="请选择调入部门" style="width:100%">
            <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入位置">
          <el-input v-model="applyForm.toLocation" placeholder="如：3楼机房" />
        </el-form-item>
        <el-form-item label="调入使用人">
          <el-input v-model="applyForm.toUser" placeholder="如：张三" />
        </el-form-item>
        <el-form-item label="调拨原因" prop="transferReason">
          <el-input v-model="applyForm.transferReason" type="textarea" :rows="3" placeholder="请说明调拨原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="调拨详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="调拨单号">{{ currentRow.transferNo }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ currentRow.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentRow.assetCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTag(currentRow.transferStatus)">{{ statusText(currentRow.transferStatus) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="调出部门">{{ currentRow.fromDepartment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调入部门">{{ currentRow.toDepartment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调出位置">{{ currentRow.fromLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调入位置">{{ currentRow.toLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调出使用人">{{ currentRow.fromUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调入使用人">{{ currentRow.toUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="调拨原因" :span="2">{{ currentRow.transferReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2">{{ currentRow.approveRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRow.approveTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const assetList = ref([])
const deptList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchStatus = ref(null)
const applyVisible = ref(false)
const detailVisible = ref(false)
const currentRow = ref(null)
const applyFormRef = ref(null)

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))

const applyForm = reactive({
  assetId: null, toDepartment: '', toLocation: '', toUser: '', transferReason: ''
})

const applyRules = reactive({
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  toDepartment: [{ required: true, message: '请填写调入部门', trigger: 'blur' }],
  transferReason: [{ required: true, message: '请填写调拨原因', trigger: 'blur' }]
})

const statusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const statusText = (s) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' }[s] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/transfer/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value, status: searchStatus.value } })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const loadAssets = async () => {
  try {
    const res = await request.get('/assetInfo/list', { params: { status: 1 } })
    assetList.value = res.data || []
  } catch { /* ignore */ }
}

const showApply = async () => {
  await loadAssets()
  if (assetList.value.length === 0) { ElMessage.warning('没有可用资产'); return }
  applyFormRef.value?.resetFields()
  applyVisible.value = true
}

const resetApplyForm = () => Object.assign(applyForm, { assetId: null, toDepartment: '', toLocation: '', toUser: '', transferReason: '' })

const submitApply = async () => {
  if (!applyFormRef.value) return
  try {
    await applyFormRef.value.validate()
    submitting.value = true
    const res = await request.post('/transfer/apply', applyForm)
    if (res.code === 200) {
      ElMessage.success('调拨申请已提交')
      applyVisible.value = false
      loadData()
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || '提交失败') } finally { submitting.value = false }
}

const handleApprove = async (row, status) => {
  try {
    await ElMessageBox.confirm(`确定${status === 1 ? '通过' : '拒绝'}该调拨申请？`, '确认', { type: 'warning' })
    const res = await request.post('/transfer/approve', null, { params: { transferId: row.transferId, status } })
    if (res.code === 200) { ElMessage.success('操作成功'); loadData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || '操作失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该调拨记录？', '确认', { type: 'warning' })
    const res = await request.post('/transfer/delete', null, { params: { transferId: row.transferId } })
    if (res.code === 200) { ElMessage.success('已删除'); loadData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const showDetail = (row) => { currentRow.value = row; detailVisible.value = true }

const loadDepts = async () => {
  try {
    const res = await request.get('/department/list')
    if (res.code === 200) deptList.value = res.data || []
  } catch { /* ignore */ }
}

onMounted(() => { loadData(); loadDepts() })
</script>

<style scoped>
.transfer-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { min-height: 400px; }
</style>
