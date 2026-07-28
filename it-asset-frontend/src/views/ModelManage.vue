<template>
  <div class="page-container">
    <div class="header-title">资产模型管理</div>

    <div class="operation-section">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 新增模型
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="modelList" v-loading="loading" border stripe style="width:100%">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="modelName" label="模型名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="modelNumber" label="模型编号" width="120" align="center" />
        <el-table-column label="所属分类" width="120" align="center">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="制造商" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="specs" label="规格说明" min-width="180" show-overflow-tooltip />
        <el-table-column prop="eolMonths" label="EOL周期(月)" width="100" align="center" />
        <el-table-column prop="depreciationYears" label="折旧年限" width="90" align="center" />
        <el-table-column label="折旧方法" width="110" align="center">
          <template #default="{ row }">
            {{ getDepMethodText(row.depreciationMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '新增模型'" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="如: Dell OptiPlex 7090" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型编号">
              <el-input v-model="form.modelNumber" placeholder="如: DELL-OP-7090" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属分类">
              <el-select v-model="form.categoryId" placeholder="选择分类" clearable style="width:100%">
                <el-option v-for="c in categoryList" :key="c.categoryId" :label="c.categoryName" :value="c.categoryId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="制造商">
              <el-input v-model="form.manufacturer" placeholder="如: Dell" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规格说明">
          <el-input v-model="form.specs" type="textarea" :rows="2" placeholder="如: CPU: i7-11700, RAM: 16GB, SSD: 512GB" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="EOL周期">
              <el-input-number v-model="form.eolMonths" :min="1" :max="120" style="width:100%" />
              <div style="font-size:12px;color:#999">单位：月</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折旧年限">
              <el-input-number v-model="form.depreciationYears" :min="1" :max="20" style="width:100%" />
              <div style="font-size:12px;color:#999">单位：年</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折旧方法">
              <el-select v-model="form.depreciationMethod" style="width:100%">
                <el-option label="直线折旧" value="straight_line" />
                <el-option label="余额递减" value="declining_balance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const modelList = ref([])
const categoryList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  modelId: null,
  modelName: '',
  modelNumber: '',
  categoryId: null,
  manufacturer: '',
  specs: '',
  eolMonths: 36,
  depreciationYears: 3,
  depreciationMethod: 'straight_line',
  remark: ''
})

const rules = reactive({
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }]
})

onMounted(() => {
  getCategoryList()
  getList()
})

const getCategoryList = async () => {
  try {
    const res = await request.get('/category/list')
    categoryList.value = res.data || []
  } catch (e) { /* silent */ }
}

const getCategoryName = (id) => {
  const c = categoryList.value.find(c => c.categoryId === id)
  return c ? c.categoryName : ''
}

const getDepMethodText = (m) => {
  if (m === 'straight_line') return '直线折旧'
  if (m === 'declining_balance') return '余额递减'
  return m || '-'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await request.get('/assetModel/page', {
      params: { current: pagination.current, size: pagination.size }
    })
    modelList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    modelId: null, modelName: '', modelNumber: '', categoryId: null,
    manufacturer: '', specs: '', eolMonths: 36, depreciationYears: 3,
    depreciationMethod: 'straight_line', remark: ''
  })
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
      await request.post('/assetModel/update', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/assetModel/save', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除模型"${row.modelName}"？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/assetModel/delete', null, { params: { modelId: row.modelId } })
      ElMessage.success('删除成功')
      getList()
    } catch (e) { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
}
</script>

<style scoped>
</style>
