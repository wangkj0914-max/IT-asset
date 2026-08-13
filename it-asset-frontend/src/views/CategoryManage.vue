<template>
  <div class="page-container">
    <div class="header-title">
      <span>资产分类管理</span>
      <el-button v-if="userRole === 2" type="primary" size="small" @click="showAddDialog(null)">
        <el-icon><Plus /></el-icon> 新增父级分类
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="treeData" v-loading="loading" border stripe row-key="categoryId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
        <el-table-column prop="categoryName" label="分类名称" min-width="220">
          <template #default="{ row }">
            <span v-if="row.parentId === 0" style="font-weight:bold;color:#303133">
              <el-icon style="margin-right:4px"><FolderOpened /></el-icon>{{ row.categoryName }}
            </span>
            <span v-else style="padding-left:8px;color:#606266">{{ row.categoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="层级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.parentId === 0 ? 'primary' : 'info'" size="small">
              {{ row.parentId === 0 ? '父级' : '子级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.parentId === 0 && userRole === 2" type="success" size="small" @click="showAddDialog(row)">
              <el-icon><Plus /></el-icon> 子分类
            </el-button>
            <el-button v-if="userRole === 2" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="userRole === 2" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && categoryList.length === 0" description="暂无分类" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form :model="categoryForm" :rules="formRules" ref="categoryFormRef" label-width="100px">
        <template v-if="categoryForm.parentId > 0">
          <el-form-item label="父级分类">
            <el-input :value="selectedParentName" disabled />
          </el-form-item>
          <el-form-item label="分类名称" prop="categoryName">
            <el-input v-model="categoryForm.categoryName" placeholder="如：服务器" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="分类名称" prop="categoryName">
            <el-input v-model="categoryForm.categoryName" placeholder="如：固定资产" />
          </el-form-item>
          <el-form-item label="父级分类" prop="parentId">
            <el-select v-model="categoryForm.parentId" placeholder="请选择" style="width:100%">
              <el-option label="顶级（父级分类）" :value="0" />
              <el-option v-for="p in parentOptions" :key="p.categoryId" :label="p.categoryName" :value="p.categoryId"
                :disabled="isEditMode && p.categoryId === categoryForm.categoryId" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" style="width:100%" />
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
import { Plus, FolderOpened } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const loading = ref(false), dialogVisible = ref(false), isEditMode = ref(false)
const categoryList = ref([]), categoryFormRef = ref(null)

const categoryForm = reactive({ categoryId: null, categoryName: '', parentId: 0, sortOrder: 1 })
const formRules = reactive({ categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] })

const parentOptions = computed(() => categoryList.value.filter(c => c.parentId === 0))

const selectedParentName = computed(() => {
  if (categoryForm.parentId <= 0) return ''
  const p = categoryList.value.find(c => c.categoryId === categoryForm.parentId)
  return p ? p.categoryName : ''
})

const dialogTitle = computed(() => {
  if (isEditMode.value) return '编辑分类'
  return categoryForm.parentId > 0 ? '新增子分类' : '新增父级分类'
})

const treeData = computed(() => {
  const list = categoryList.value
  const parents = list.filter(c => c.parentId === 0).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
  return parents.map(p => {
    const children = list.filter(c => c.parentId === p.categoryId).sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    return { ...p, children, hasChildren: children.length > 0 }
  })
})

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await request.get('/category/list')
    categoryList.value = res.data || []
  } catch { ElMessage.error('加载分类失败') } finally { loading.value = false }
}

const showAddDialog = (parentRow) => {
  isEditMode.value = false
  Object.assign(categoryForm, {
    categoryId: null, categoryName: '',
    parentId: parentRow ? parentRow.categoryId : 0,
    sortOrder: 1
  })
  categoryFormRef.value?.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEditMode.value = true
  Object.assign(categoryForm, {
    categoryId: row.categoryId, categoryName: row.categoryName,
    parentId: row.parentId || 0, sortOrder: row.sortOrder || 1
  })
  categoryFormRef.value?.clearValidate()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  try {
    await categoryFormRef.value.validate()
    const url = isEditMode.value ? '/category/update' : '/category/save'
    await request.post(url, categoryForm)
    ElMessage.success(isEditMode.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    loadCategories()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleDelete = (row) => {
  const hasChildren = categoryList.value.some(c => c.parentId === row.categoryId)
  const msg = hasChildren ? `「${row.categoryName}」下有子分类，删除后子分类将变为顶级分类，确认删除？` : `确认删除「${row.categoryName}」？`
  ElMessageBox.confirm(msg, '确认', { type: 'warning' }).then(async () => {
    try {
      await request.post('/category/delete', null, { params: { categoryId: row.categoryId } })
      ElMessage.success('删除成功')
      loadCategories()
    } catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

onMounted(loadCategories)
</script>

<style scoped>
</style>
