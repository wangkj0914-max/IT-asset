<template>
  <div class="page-container">
    <div class="header-title">合同管理</div>
    <div class="operation-section" v-if="userRole === 2">
      <el-input v-model="searchKey" placeholder="搜索合同名称/编号/供应商" clearable @keyup.enter="loadData" @clear="loadData" style="width:280px;margin-right:8px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 新增合同
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" border stripe style="width:100%">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="contractNo" label="合同编号" width="140" />
        <el-table-column prop="contractName" label="合同名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="supplier" label="供应商" width="150" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="110" align="right">
          <template #default="{ row }">¥{{ row.amount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="signDate" label="签订日期" width="110" align="center" />
        <el-table-column prop="expiryDate" label="到期日期" width="110" align="center">
          <template #default="{ row }">
            <span :style="{ color: isExpiringSoon(row) ? '#DC2626' : '' }">
              {{ row.expiryDate || '-' }}
            </span>
            <el-tag v-if="isExpiringSoon(row)" type="danger" size="small" style="margin-left:4px">临期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center" fixed="right" v-if="userRole === 2">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :page-sizes="[10,20,50]"
          :total="page.total" layout="total,sizes,prev,pager,next" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑合同' : '新增合同'" width="550px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="合同编号" prop="contractNo">
          <el-input v-model="form.contractNo" placeholder="选填，留空自动生成" />
        </el-form-item>
        <el-form-item label="合同名称" prop="contractName">
          <el-input v-model="form.contractName" placeholder="请输入合同名称" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input-number v-model="form.amount" :precision="2" :min="0" style="width:100%" placeholder="请输入金额" />
        </el-form-item>
        <el-form-item label="签订日期">
          <el-date-picker v-model="form.signDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker v-model="form.expiryDate" type="date" placeholder="选择到期日期" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="履行中" :value="0" />
            <el-option label="已到期" :value="1" />
            <el-option label="已终止" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const loading = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const tableData = ref([]), searchKey = ref(''), formRef = ref(null)
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ contractId: null, contractNo: '', contractName: '', supplier: '', amount: null, signDate: '', expiryDate: '', status: 0, remark: '' })
const rules = {
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

const statusText = (s) => ({ 0: '履行中', 1: '已到期', 2: '已终止' }[s] || '未知')
const statusTag = (s) => ({ 0: 'success', 1: 'danger', 2: 'info' }[s] || '')
const isExpiringSoon = (row) => {
  if (!row.expiryDate || row.status !== 0) return false
  const diff = new Date(row.expiryDate) - new Date()
  return diff > 0 && diff < 30 * 24 * 3600 * 1000
}

const loadData = async () => {
  loading.value = true
  const res = await request.get('/contract/page', {
    params: { current: page.current, size: page.size, keyword: searchKey.value || undefined }
  })
  tableData.value = res.data.records || []
  page.total = res.data.total || 0
  loading.value = false
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { contractId: null, contractNo: '', contractName: '', supplier: '', amount: null, signDate: '', expiryDate: '', status: 0, remark: '' })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row, amount: row.amount })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await request.post('/contract/save', { ...form, site: localStorage.getItem('site') || '' })
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch { /* validation error */ }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除合同"${row.contractName}"？`, '提示', { type: 'warning' }).then(async () => {
    await request.post('/contract/delete', null, { params: { contractId: row.contractId } })
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const resetForm = () => formRef.value?.resetFields()

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 20px; }
.header-title { font-size: 20px; font-weight: bold; color: #1A1A2E; margin-bottom: 16px; }
.operation-section { margin-bottom: 16px; display: flex; align-items: center; }
.table-section { background: white; border-radius: 8px; padding: 16px; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
