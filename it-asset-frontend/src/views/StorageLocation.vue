<template>
  <div class="page-container">
    <div class="header-title">
      <span>存放地点管理</span>
      <el-button type="primary" size="small" @click="showAdd" v-if="userRole === 2">
        <el-icon><Plus /></el-icon> 新增地点
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="locationId" label="ID" width="80" align="center" />
        <el-table-column prop="locationName" label="地点名称" min-width="200" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="160" align="center" v-if="userRole === 2">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地点' : '新增地点'" width="420px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="地点名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="如：SPO2车间" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注信息" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const loading = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const tableData = ref([]), formRef = ref(null)
const form = reactive({ locationId: null, locationName: '', remark: '', sortOrder: 1 })
const rules = reactive({ locationName: [{ required: true, message: '请输入名称', trigger: 'blur' }] })

const loadData = async () => {
  loading.value = true
  try { const r = await request.get('/storage-location/list'); if (r.code === 200) tableData.value = r.data || [] }
  catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

const showAdd = () => { isEdit.value = false; Object.assign(form, { locationId: null, locationName: '', remark: '', sortOrder: 1 }); formRef.value?.clearValidate(); dialogVisible.value = true }
const showEdit = (row) => { isEdit.value = true; Object.assign(form, { locationId: row.locationId, locationName: row.locationName, remark: row.remark, sortOrder: row.sortOrder }); formRef.value?.clearValidate(); dialogVisible.value = true }

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const url = isEdit.value ? '/storage-location/update' : '/storage-location/save'
    const r = await request.post(url, form)
    if (r.code === 200) { ElMessage.success(isEdit.value ? '更新成功' : '添加成功'); dialogVisible.value = false; loadData() }
  } catch { /* validation error */ }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.locationName}」？`, '确认', { type: 'warning' }).then(async () => {
    const r = await request.post('/storage-location/delete', null, { params: { locationId: row.locationId } })
    if (r.code === 200) { ElMessage.success('已删除'); loadData() }
  }).catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
.page-container { width: 95%; margin: 0 auto; padding: 20px; }
.header-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white; padding: 16px 24px; font-size: 18px; font-weight: bold;
  border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center;
  justify-content: space-between; box-shadow: 0 2px 12px rgba(102,126,234,0.3);
}
.table-section { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
</style>
