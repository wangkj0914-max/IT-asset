<template>
  <div class="page-container">
    <div class="header-title">
      <span>{{ t('ui.用户管理') }}</span>
      <div>
        <el-input
          v-model="searchKeyword"
          :placeholder="t('ui.搜索用户名/姓名')"
          style="width: 200px; margin-right: 10px;"
          clearable
          @clear="loadUsers"
          @keyup.enter="loadUsers"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="small" @click="showAddDialog" v-if="userRole === 2">
          <el-icon><Plus /></el-icon> {{ t('ui.新增用户') }}
        </el-button>
      </div>
    </div>

    <div class="table-section">
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="userId" label="ID" width="70" align="center" />
        <el-table-column prop="username" :label="t('ui.用户名')" width="130" align="center" />
        <el-table-column prop="realName" :label="t('ui.姓名')" width="120" align="center" />
        <el-table-column prop="department" :label="t('ui.部门')" width="130" align="center">
          <template #default="{ row }">
            {{ row.department || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" :label="t('ui.电话')" width="130" align="center">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="t('ui.邮箱')" min-width="180">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="t('ui.角色')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : 'info'" size="small">
              {{ row.role === 2 ? t('ui.管理员') : t('ui.普通用户') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('ui.状态')" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? t('ui.正常') : t('ui.禁用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('ui.创建时间')" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('ui.操作')" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)" v-if="userRole === 2">{{ t('ui.编辑') }}</el-button>
            <el-button type="warning" size="small" @click="handleResetPwd(row)" v-if="userRole === 2">{{ t('ui.重置密码') }}</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" v-if="userRole === 2">{{ t('ui.删除') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && userList.length === 0" :description="t('ui.暂无数据')" />

      <div class="pagination-section" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadUsers"
          @size-change="loadUsers"
        />
      </div>
    </div>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? t('ui.编辑用户') : t('ui.新增用户')" width="500px" @close="resetForm">
      <el-form :model="userForm" :rules="formRules" ref="userFormRef" label-width="90px">
        <el-form-item :label="t('ui.用户名')" prop="username">
          <el-input v-model="userForm.username" :placeholder="t('ui.请输入用户名')" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item v-if="!isEditMode" :label="t('ui.密码')" prop="password">
          <el-input v-model="userForm.password" type="password" :placeholder="t('ui.请输入密码（默认123456）')" show-password />
        </el-form-item>
        <el-form-item :label="t('ui.姓名')" prop="realName">
          <el-input v-model="userForm.realName" :placeholder="t('ui.请输入真实姓名')" />
        </el-form-item>
        <el-form-item :label="t('ui.部门')" prop="department">
          <el-select v-model="userForm.department" :placeholder="t('ui.请选择部门')" style="width: 100%;" clearable filterable>
            <el-option v-for="dept in departmentList" :key="dept.deptId" :label="dept.deptName" :value="dept.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('ui.电话')" prop="phone">
          <el-input v-model="userForm.phone" :placeholder="t('ui.请输入联系电话')" />
        </el-form-item>
        <el-form-item :label="t('ui.邮箱')" prop="email">
          <el-input v-model="userForm.email" :placeholder="t('ui.请输入邮箱')" />
        </el-form-item>
        <el-form-item :label="t('ui.角色')" prop="role">
          <el-radio-group v-model="userForm.role">
            <el-radio :value="1">{{ t('ui.普通用户') }}</el-radio>
            <el-radio :value="2">{{ t('ui.管理员') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="t('ui.状态')" prop="status">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" :active-text="t('ui.正常')" :inactive-text="t('ui.禁用')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('ui.取消') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">{{ t('ui.确定') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))

const loading = ref(false)
const submitLoading = ref(false)
const userList = ref([])
const departmentList = ref([])
const dialogVisible = ref(false)
const isEditMode = ref(false)
const userFormRef = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const userForm = reactive({
  userId: null,
  username: '',
  password: '123456',
  realName: '',
  department: '',
  phone: '',
  email: '',
  role: 1,
  status: 1
})

const formRules = reactive({
  username: [
    { required: true, message: t('ui.请输入用户名'), trigger: 'blur' },
    { min: 2, max: 20, message: t('ui.用户名长度 2-20 个字符'), trigger: 'blur' }
  ],
  password: [
    { min: 6, message: t('ui.密码长度不能少于 6 个字符'), trigger: 'blur' }
  ],
  realName: [
    { required: true, message: t('ui.请输入姓名'), trigger: 'blur' }
  ]
})

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const d = new Date(timeStr)
  const pad = n => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list', {
      params: {
        current: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value || undefined
      }
    })
    userList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error(t('ui.加载用户列表失败'))
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    const res = await request.get('/department/list')
    departmentList.value = res.data || []
  } catch (error) {
    // 部门列表加载失败不阻塞页面
  }
}

const showAddDialog = () => {
  isEditMode.value = false
  Object.assign(userForm, {
    userId: null,
    username: '',
    password: '123456',
    realName: '',
    department: '',
    phone: '',
    email: '',
    role: 1,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEditMode.value = true
  Object.assign(userForm, {
    userId: row.userId,
    username: row.username,
    password: '',
    realName: row.realName || '',
    department: row.department || '',
    phone: row.phone || '',
    email: row.email || '',
    role: row.role || 1,
    status: row.status !== undefined ? row.status : 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  try {
    await userFormRef.value.validate()
    submitLoading.value = true

    if (isEditMode.value) {
      await request.post('/user/update', userForm)
      ElMessage.success(t('ui.用户信息更新成功'))
    } else {
      await request.post('/user/create', userForm)
      ElMessage.success(t('ui.用户创建成功'))
    }
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.msg || (isEditMode.value ? t('ui.更新失败') : t('ui.创建失败')))
    }
  } finally {
    submitLoading.value = false
  }
}

const handleResetPwd = (row) => {
  ElMessageBox.confirm(
    t('ui.确认要重置用户 {name} 的密码吗？密码将重置为 123456', { name: row.realName || row.username }),
    t('ui.重置密码'),
    { confirmButtonText: t('ui.确定'), cancelButtonText: t('ui.取消'), type: 'warning' }
  ).then(async () => {
    try {
      await request.post('/user/reset-password', null, { params: { userId: row.userId } })
      ElMessage.success(t('ui.密码已重置为 123456'))
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || t('ui.重置失败'))
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  if (row.username === 'admin') {
    ElMessage.warning(t('ui.不能删除管理员账号'))
    return
  }
  ElMessageBox.confirm(
    t('ui.确认要删除用户 {name} 吗？删除后不可恢复！', { name: row.realName || row.username }),
    t('ui.删除确认'),
    { confirmButtonText: t('ui.确定'), cancelButtonText: t('ui.取消'), type: 'warning' }
  ).then(async () => {
    try {
      await request.post('/user/delete', null, { params: { userId: row.userId } })
      ElMessage.success(t('ui.删除成功'))
      if (userList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      loadUsers()
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || t('ui.删除失败'))
    }
  }).catch(() => {})
}

const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

onMounted(() => {
  loadUsers()
  loadDepartments()
})
</script>

<style scoped>
</style>
