<template>
  <div class="page-container">
    <div class="header-title">个人信息</div>
    
    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="12">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>📋 基本信息</span>
              <el-button type="primary" size="small" @click="showEditInfoDialog">修改信息</el-button>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userInfo.realName }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userInfo.role === 2 ? 'danger' : 'info'" size="large">
                {{ userInfo.role === 2 ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <!-- 修改角色（仅管理员可操作） -->
      <el-col :span="12">
        <el-card class="role-card">
          <template #header>
            <div class="card-header">
              <span>🔐 权限设置</span>
            </div>
          </template>
          <el-form label-width="100px">
            <el-form-item label="当前角色">
              <el-tag :type="userInfo.role === 2 ? 'danger' : 'info'">
                {{ userInfo.role === 2 ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="设置角色" v-if="userInfo.role === 2">
              <el-select v-model="targetRole" placeholder="请选择角色" style="width: 200px;">
                <el-option label="管理员" :value="2" />
                <el-option label="普通用户" :value="1" />
              </el-select>
              <el-button type="primary" @click="handleChangeRole" style="margin-left: 10px;">
                修改
              </el-button>
            </el-form-item>
            <el-alert
              v-else
              title="仅管理员可以修改用户角色"
              type="warning"
              :closable="false"
              show-icon
            />
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 用户列表（仅管理员可见） -->
    <el-card class="user-list-card" v-if="userInfo.role === 2">
      <template #header>
        <div class="card-header">
          <span>👥 用户管理</span>
          <el-button type="primary" size="small" @click="showAddUserDialog">
            <el-icon><Plus /></el-icon> 新增用户
          </el-button>
        </div>
      </template>
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" align="center" />
        <el-table-column prop="realName" label="姓名" width="120" align="center" />
        <el-table-column label="角色" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 2 ? 'danger' : 'info'" size="small">
              {{ row.role === 2 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="showEditRoleDialog(row)"
              v-if="row.userId !== userInfo.userId"
            >
              修改角色
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeleteUser(row)"
              v-if="row.userId !== userInfo.userId"
            >
              删除
            </el-button>
            <span v-else style="color: #909399; font-size: 13px;">当前用户</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && userList.length === 0" description="暂无用户数据" />
    </el-card>
    
    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="editInfoDialogVisible" title="修改个人信息" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <span>{{ userInfo.username }}</span>
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="editForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editInfoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateInfo">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 修改角色对话框 -->
    <el-dialog v-model="editRoleDialogVisible" title="修改用户角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ currentUser.username }}</span>
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="currentUser.role === 2 ? 'danger' : 'info'">
            {{ currentUser.role === 2 ? '管理员' : '普通用户' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="targetRole" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" :value="2" />
            <el-option label="普通用户" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEditRole">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 新增用户对话框 -->
    <el-dialog v-model="addDialogVisible" title="新增用户" width="500px">
      <el-form :model="newUser" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="newUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="newUser.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码（默认 123456）" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newUser.role" style="width: 100%;">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="newUser.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="newUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="newUser.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const userInfo = reactive({
  userId: null,
  username: '',
  realName: '',
  role: 1
})

const loading = ref(false)
const userList = ref([])
const targetRole = ref(1)
const editRoleDialogVisible = ref(false)
const editInfoDialogVisible = ref(false)
const addDialogVisible = ref(false)
const currentUser = reactive({
  userId: null,
  username: '',
  role: 1
})

const editForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const newUser = reactive({
  username: '',
  realName: '',
  password: '',
  role: 1,
  department: '',
  email: '',
  phone: ''
})

const loadUserInfo = () => {
  userInfo.userId = parseInt(localStorage.getItem('userId') || '1')
  userInfo.username = localStorage.getItem('username') || ''
  userInfo.realName = localStorage.getItem('realName') || ''
  userInfo.role = parseInt(localStorage.getItem('role') || '1')
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list', {
      params: { current: 1, size: 100 }
    })
    userList.value = res.data.records || []
  } catch (error) {
    ElMessage.error('加载用户列表失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

const handleChangeRole = () => {
  if (targetRole.value === userInfo.role) {
    ElMessage.warning('请选择不同的角色')
    return
  }
  
  ElMessageBox.confirm(
    `确认要将自己的角色修改为${targetRole.value === 2 ? '管理员' : '普通用户'}吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post('/user/update-role', null, {
        params: { userId: userInfo.userId, role: targetRole.value }
      })
      userInfo.role = targetRole.value
      localStorage.setItem('role', targetRole.value.toString())
      ElMessage.success('角色修改成功，请重新登录')
      setTimeout(() => {
        localStorage.removeItem('token')
        router.push('/')
      }, 1500)
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || '修改失败')
    }
  }).catch(() => {})
}

const showEditRoleDialog = (row) => {
  currentUser.userId = row.userId
  currentUser.username = row.username
  currentUser.role = row.role
  targetRole.value = row.role
  editRoleDialogVisible.value = true
}

const confirmEditRole = async () => {
  if (targetRole.value === currentUser.role) {
    ElMessage.warning('请选择不同的角色')
    return
  }
  
  try {
    await request.post('/user/update-role', null, {
      params: { userId: currentUser.userId, role: targetRole.value }
    })
    
    const user = userList.value.find(u => u.userId === currentUser.userId)
    if (user) {
      user.role = targetRole.value
    }
    
    editRoleDialogVisible.value = false
    ElMessage.success('用户角色修改成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '修改失败')
  }
}

const showAddUserDialog = () => {
  Object.assign(newUser, {
    username: '',
    realName: '',
    password: '',
    role: 1,
    department: '',
    email: '',
    phone: ''
  })
  addDialogVisible.value = true
}

const handleCreateUser = async () => {
  if (!newUser.username || !newUser.realName) {
    ElMessage.warning('用户名和姓名为必填项')
    return
  }
  
  try {
    await request.post('/user/create', newUser)
    ElMessage.success('用户创建成功')
    addDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '创建失败')
  }
}

const handleDeleteUser = (row) => {
  ElMessageBox.confirm(
    `确认要删除用户"${row.username}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post('/user/delete', null, {
        params: { userId: row.userId }
      })
      userList.value = userList.value.filter(u => u.userId !== row.userId)
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }).catch(() => {})
}

const showEditInfoDialog = () => {
  editForm.realName = userInfo.realName || ''
  editForm.email = ''
  editForm.phone = ''
  editInfoDialogVisible.value = true
}

const handleUpdateInfo = async () => {
  if (!editForm.realName) {
    ElMessage.warning('姓名不能为空')
    return
  }
  
  try {
    await request.post('/user/update', {
      userId: userInfo.userId,
      realName: editForm.realName,
      email: editForm.email,
      phone: editForm.phone
    })
    
    userInfo.realName = editForm.realName
    localStorage.setItem('realName', editForm.realName)
    
    editInfoDialogVisible.value = false
    ElMessage.success('信息更新成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '更新失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadUsers()
})
</script>

<style scoped>
.page-container {
  width: 95%;
  margin: 0 auto;
  padding: 20px;
}

.header-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 8px;
  margin-bottom: 20px;
  width: 100%;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.profile-card, .role-card {
  background: white;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.user-list-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  font-weight: 500;
}
</style>
