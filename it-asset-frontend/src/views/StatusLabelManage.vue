<template>
  <div class="page-container">
    <div class="header-title">状态标签管理</div>

    <div class="operation-section">
      <el-button v-if="userRole === 2" type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 新增状态
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="statusList" v-loading="loading" border stripe style="width:100%">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column label="状态名称" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.color" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态类型" width="140" align="center">
          <template #default="{ row }">
            {{ getTypeText(row.statusType) }}
          </template>
        </el-table-column>
        <el-table-column label="颜色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.color" size="small">{{ row.color }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userRole === 2" type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="userRole === 2" type="danger" size="small" @click="handleDelete(row)" :disabled="row.isDefault === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑状态' : '新增状态'" width="480px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="状态名称" prop="statusName">
          <el-input v-model="form.statusName" placeholder="如: 待部署" clearable />
        </el-form-item>
        <el-form-item label="状态类型" prop="statusType">
          <el-select v-model="form.statusType" style="width:100%">
            <el-option label="可部署" :value="0" />
            <el-option label="已部署" :value="1" />
            <el-option label="不可部署" :value="2" />
            <el-option label="已归档" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-select v-model="form.color" style="width:100%">
            <el-option label="primary (蓝)" value="primary" />
            <el-option label="success (绿)" value="success" />
            <el-option label="warning (橙)" value="warning" />
            <el-option label="danger (红)" value="danger" />
            <el-option label="info (灰)" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const loading = ref(false)
const statusList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  statusLabelId: null,
  statusName: '',
  statusType: 0,
  color: 'primary',
  isDefault: 0,
  remark: ''
})

const rules = reactive({
  statusName: [{ required: true, message: '请输入状态名称', trigger: 'blur' }],
  statusType: [{ required: true, message: '请选择状态类型', trigger: 'change' }]
})

onMounted(() => { getList() })

const getTypeText = (t) => {
  const map = { 0: '可部署', 1: '已部署', 2: '不可部署', 3: '已归档' }
  return map[t] || '-'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/statusLabel/list')
    statusList.value = res.data || []
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, { statusLabelId: null, statusName: '', statusType: 0, color: 'primary', isDefault: 0, remark: '' })
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await request.post('/statusLabel/update', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/statusLabel/save', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除状态"${row.statusName}"？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await request.post('/statusLabel/delete', null, { params: { statusLabelId: row.statusLabelId } })
        ElMessage.success('删除成功')
        getList()
      } catch (e) { ElMessage.error('删除失败') }
    }).catch(() => {})
}

const resetForm = () => { if (formRef.value) formRef.value.resetFields() }
</script>

<style scoped>
</style>
