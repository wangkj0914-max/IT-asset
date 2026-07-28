<template>
  <div class="page-container">
    <div class="header-title">
      <span>自定义字段管理</span>
      <el-button type="primary" size="small" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 新增字段
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="fieldList" v-loading="loading" border stripe>
        <el-table-column prop="fieldName" label="字段名称" min-width="160" />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeLabel(row.fieldType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.isRequired === 1 ? '#F56C6C' : '#909399' }">
              {{ row.isRequired === 1 ? '是' : '否' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && fieldList.length === 0" description="暂无自定义字段" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? '编辑字段' : '新增字段'" width="500px" @close="resetForm">
      <el-form :model="fieldForm" :rules="formRules" ref="fieldFormRef" label-width="100px">
        <el-form-item label="字段名称" prop="fieldName">
          <el-input v-model="fieldForm.fieldName" placeholder="如：MAC地址、操作系统" clearable />
        </el-form-item>
        <el-form-item label="字段类型" prop="fieldType">
          <el-select v-model="fieldForm.fieldType" placeholder="请选择类型" style="width:100%" @change="onTypeChange">
            <el-option label="文本" value="text" />
            <el-option label="数字" value="number" />
            <el-option label="日期" value="date" />
            <el-option label="下拉选择" value="select" />
            <el-option label="多行文本" value="textarea" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="fieldForm.fieldType === 'select'" label="下拉选项">
          <el-input v-model="optionsInput" placeholder="选项1,选项2,选项3" clearable />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">多个选项用英文逗号分隔</div>
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="fieldForm.isRequired" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="fieldForm.sortOrder" :min="0" :max="999" style="width:100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const fieldList = ref([])
const dialogVisible = ref(false)
const isEditMode = ref(false)
const fieldFormRef = ref(null)
const optionsInput = ref('')

const fieldForm = reactive({
  fieldId: null,
  fieldName: '',
  fieldType: 'text',
  isRequired: 0,
  sortOrder: 0
})

const formRules = reactive({
  fieldName: [{ required: true, message: '请输入字段名称', trigger: 'blur' }],
  fieldType: [{ required: true, message: '请选择字段类型', trigger: 'change' }]
})

const typeLabelMap = {
  text: '文本',
  number: '数字',
  date: '日期',
  select: '下拉选择',
  textarea: '多行文本'
}

const getTypeLabel = (type) => typeLabelMap[type] || type

const onTypeChange = () => {
  if (fieldForm.fieldType !== 'select') {
    optionsInput.value = ''
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const r = await request.get('/custom-field/def-list', { params: { targetEntity: 'asset' } })
    if (r.code === 200) fieldList.value = r.data || []
  } catch {
    fieldList.value = []
  } finally {
    loading.value = false
  }
}

// 将逗号分隔的字符串转为 JSON 数组格式，用于 AssetManage.vue 的 parseOptions
const optionsToJson = (input) => {
  if (!input || !input.trim()) return null
  const arr = input.split(',').map(s => s.trim()).filter(s => s)
  return JSON.stringify(arr)
}

const optionsFromJson = (jsonStr) => {
  if (!jsonStr) return ''
  try {
    const arr = JSON.parse(jsonStr)
    return Array.isArray(arr) ? arr.join(',') : jsonStr
  } catch {
    return jsonStr
  }
}

const showAddDialog = () => {
  isEditMode.value = false
  fieldForm.fieldId = null
  fieldForm.fieldName = ''
  fieldForm.fieldType = 'text'
  fieldForm.isRequired = 0
  fieldForm.sortOrder = 0
  optionsInput.value = ''
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEditMode.value = true
  fieldForm.fieldId = row.fieldId
  fieldForm.fieldName = row.fieldName
  fieldForm.fieldType = row.fieldType
  fieldForm.isRequired = row.isRequired || 0
  fieldForm.sortOrder = row.sortOrder || 0
  optionsInput.value = optionsFromJson(row.fieldOptions)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!fieldFormRef.value) return
  try {
    await fieldFormRef.value.validate()

    const data = {
      fieldId: fieldForm.fieldId || null,
      fieldName: fieldForm.fieldName,
      fieldType: fieldForm.fieldType,
      isRequired: fieldForm.isRequired,
      sortOrder: fieldForm.sortOrder
    }

    // 下拉类型处理选项
    if (fieldForm.fieldType === 'select') {
      data.fieldOptions = optionsToJson(optionsInput.value)
    } else {
      data.fieldOptions = null
    }

    await request.post('/custom-field/def-save', data)
    ElMessage.success(isEditMode.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(isEditMode.value ? '更新失败' : '新增失败')
    }
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认要删除字段"${row.fieldName}"吗？删除后该字段的所有数据也将被清除。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/custom-field/def-delete', null, { params: { fieldId: row.fieldId } })
      ElMessage.success('删除成功')
      loadList()
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (fieldFormRef.value) {
    fieldFormRef.value.resetFields()
  }
  optionsInput.value = ''
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
</style>
