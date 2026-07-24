<template>
  <div class="dept-container">
    <el-card class="header-card">
      <el-row justify="space-between" align="middle">
        <span style="font-size:16px;font-weight:bold">部门列表</span>
        <el-button type="primary" @click="showAdd" v-if="userRole === 2">新增部门</el-button>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="deptId" label="ID" width="80" />
        <el-table-column prop="deptName" label="部门名称" min-width="150" />
        <el-table-column prop="deptCode" label="部门编码" width="140" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="parentId" label="上级ID" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right" v-if="userRole === 2">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无部门数据" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="如：研发部" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" placeholder="如：RD" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.manager" placeholder="如：张三" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="如：13800138000" />
        </el-form-item>
        <el-form-item label="上级ID">
          <el-input-number v-model="form.parentId" :min="0" placeholder="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="statusSwitch" active-text="正常" inactive-text="停用" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const statusSwitch = ref(1)

const form = reactive({
  deptId: null, deptName: '', deptCode: '', manager: '', phone: '',
  parentId: 0, sortOrder: 0, status: 1
})

const rules = reactive({
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/department/list')
    tableData.value = res.data || []
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const resetForm = () => {
  Object.assign(form, { deptId: null, deptName: '', deptCode: '', manager: '', phone: '', parentId: 0, sortOrder: 0, status: 1 })
  statusSwitch.value = 1
}

const showAdd = () => {
  isEdit.value = false
  resetForm()
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const showEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    deptId: row.deptId, deptName: row.deptName, deptCode: row.deptCode,
    manager: row.manager || '', phone: row.phone || '',
    parentId: row.parentId || 0, sortOrder: row.sortOrder || 0, status: row.status
  })
  statusSwitch.value = row.status
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    form.status = statusSwitch.value
    submitting.value = true
    const url = isEdit.value ? '/department/update' : '/department/save'
    const res = await request.post(url, form)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.response?.data?.msg || '操作失败') } finally { submitting.value = false }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.deptName}」？`, '确认', { type: 'warning' })
    const res = await request.post('/department/delete', null, { params: { deptId: row.deptId } })
    if (res.code === 200) { ElMessage.success('已删除'); loadData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(loadData)
</script>

<style scoped>
.dept-container { padding: 20px; }
.header-card { margin-bottom: 20px; }
.table-card { min-height: 400px; }
</style>
