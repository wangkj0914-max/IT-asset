<template>
  <div class="page-container">
    <div class="header-title">
      <span>存放地点管理</span>
      <el-button type="primary" size="small" @click="showAdd" v-if="userRole === 2">
        <el-icon><Plus /></el-icon> 新增地点
      </el-button>
    </div>

    <div class="table-section">
      <el-table :data="treeData" v-loading="loading" border stripe row-key="locationId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
        <el-table-column prop="locationName" label="地点名称" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right" v-if="userRole === 2">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && treeData.length === 0" description="暂无数据" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地点' : '新增地点'" width="420px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="地点名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="如：SPO2车间" />
        </el-form-item>
        <el-form-item label="上级地点">
          <el-select v-model="form.parentId" placeholder="无(顶级)" clearable style="width:100%">
            <el-option v-for="loc in flatList" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
          </el-select>
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
const treeData = ref([]), flatList = ref([]), formRef = ref(null)
const form = reactive({ locationId: null, locationName: '', remark: '', sortOrder: 1, parentId: null })

const rules = reactive({ locationName: [{ required: true, message: '请输入名称', trigger: 'blur' }] })

// 将树形数据拉平为带缩进前缀的选项列表
const buildFlatOptions = (nodes, excludeId) => {
  const result = []
  const walk = (list, depth) => {
    list.forEach(node => {
      if (node.locationId === excludeId) return
      const prefix = '\u00A0\u00A0'.repeat(depth) + (depth > 0 ? '\u2514 ' : '')
      result.push({ locationId: node.locationId, locationName: prefix + node.locationName })
      if (node.children && node.children.length) {
        walk(node.children, depth + 1)
      }
    })
  }
  walk(nodes, 0)
  return result
}

// Load parent options from tree data
const loadParentOptions = async (excludeId) => {
  try {
    const r = await request.get('/storage-location/tree')
    if (r.code === 200) {
      flatList.value = buildFlatOptions(r.data || [], excludeId)
    }
  } catch { flatList.value = [] }
}

const loadData = async () => {
  loading.value = true
  try {
    const r = await request.get('/storage-location/tree')
    if (r.code === 200) {
      // 为树形表格准备数据，确保每个节点都有 children 属性
      const data = r.data || []
      const addChildren = (nodes) => {
        nodes.forEach(node => {
          if (node.children && node.children.length) {
            addChildren(node.children)
          } else {
            node.children = undefined
          }
        })
      }
      addChildren(data)
      treeData.value = data
    }
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const showAdd = async () => {
  isEdit.value = false
  Object.assign(form, { locationId: null, locationName: '', remark: '', sortOrder: 1, parentId: null })
  formRef.value?.clearValidate()
  await loadParentOptions(null)
  dialogVisible.value = true
}

const showEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, { locationId: row.locationId, locationName: row.locationName, remark: row.remark, sortOrder: row.sortOrder, parentId: row.parentId || null })
  formRef.value?.clearValidate()
  await loadParentOptions(row.locationId)
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const url = isEdit.value ? '/storage-location/update' : '/storage-location/save'
    const r = await request.post(url, form)
    if (r.code === 200) { ElMessage.success(isEdit.value ? '更新成功' : '添加成功'); dialogVisible.value = false; loadData() }
    else { ElMessage.warning(r.msg || '操作失败') }
  } catch { /* validation error */ }
}

// 检查树中某节点是否有子节点
const hasChildren = (nodes, targetId) => {
  for (const node of nodes) {
    if (node.locationId === targetId) return node.children && node.children.length > 0
    if (node.children && node.children.length && hasChildren(node.children, targetId)) return true
  }
  return false
}

const handleDelete = (row) => {
  if (hasChildren(treeData.value, row.locationId)) {
    ElMessage.warning('该地点下有子级，无法删除')
    return
  }
  ElMessageBox.confirm(`确定删除「${row.locationName}」？`, '确认', { type: 'warning' }).then(async () => {
    const r = await request.post('/storage-location/delete', null, { params: { locationId: row.locationId } })
    if (r.code === 200) { ElMessage.success('已删除'); loadData() }
    else { ElMessage.warning(r.msg || '删除失败') }
  }).catch(() => {})
}

onMounted(loadData)
</script>

<style scoped>
</style>
