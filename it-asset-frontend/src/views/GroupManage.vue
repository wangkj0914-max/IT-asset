<template>
  <div class="page-container">
    <div class="header-title">
      <span>用户组管理</span>
      <div>
        <el-button type="primary" size="small" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增组
        </el-button>
      </div>
    </div>

    <div class="table-section">
      <el-table :data="groupList" v-loading="loading" border stripe>
        <el-table-column prop="groupId" label="ID" width="70" align="center" />
        <el-table-column prop="groupName" label="组名称" width="150" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="site" label="站点" width="100" align="center" />
        <el-table-column label="成员数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.memberCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.permCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="showDetail(row)">管理</el-button>
            <el-button type="primary" size="small" @click="editGroup(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteGroup(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && groupList.length === 0" description="暂无数据" />
    </div>

    <!-- 新增/编辑组对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="组名称">
          <el-input v-model="form.groupName" placeholder="请输入组名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" type="textarea" maxlength="200" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 组详情管理对话框 -->
    <el-dialog v-model="detailVisible" :title="'管理 - ' + currentGroup.groupName" width="800px" destroy-on-close>
      <el-tabs v-model="activeTab">
        <!-- 成员管理 -->
        <el-tab-pane label="组成员" name="members">
          <div style="margin-bottom: 12px; display: flex; gap: 10px;">
            <el-select
              v-model="selectedUserId"
              placeholder="选择用户添加"
              filterable
              clearable
              style="flex: 1;"
            >
              <el-option
                v-for="u in userOptions"
                :key="u.userId"
                :label="(u.realName || u.username) + ' (' + u.username + ')'"
                :value="u.userId"
              />
            </el-select>
            <el-button type="primary" @click="addUserToGroup" :disabled="!selectedUserId">
              <el-icon><Plus /></el-icon> 添加
            </el-button>
          </div>
          <el-table :data="groupUsers" v-loading="memberLoading" stripe max-height="300">
            <el-table-column prop="userId" label="ID" width="70" align="center" />
            <el-table-column prop="username" label="用户名" width="130" align="center" />
            <el-table-column prop="realName" label="姓名" width="120" align="center" />
            <el-table-column prop="department" label="部门" min-width="150" align="center">
              <template #default="{ row: r }">{{ r.department || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row: r }">
                <el-button type="danger" size="small" @click="removeUser(r)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!memberLoading && groupUsers.length === 0" description="暂无成员" />
        </el-tab-pane>

        <!-- 权限管理 -->
        <el-tab-pane label="权限设置" name="permissions">
          <el-card>
            <el-checkbox-group v-model="selectedPermissions" style="display: flex; flex-direction: column; gap: 12px;">
              <el-checkbox label="asset.view">资产查看</el-checkbox>
              <el-checkbox label="asset.edit">资产编辑</el-checkbox>
              <el-checkbox label="asset.delete">资产删除</el-checkbox>
              <el-checkbox label="asset.own_use">资产领用</el-checkbox>
              <el-checkbox label="consumable.manage">耗材管理</el-checkbox>
              <el-checkbox label="license.manage">许可证管理</el-checkbox>
              <el-checkbox label="user.manage">用户管理</el-checkbox>
              <el-checkbox label="system.admin">系统管理</el-checkbox>
            </el-checkbox-group>
            <div style="margin-top: 16px;">
              <el-button type="primary" @click="savePermissions">保存权限</el-button>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const groupList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentGroup = ref({})
const activeTab = ref('members')
const memberLoading = ref(false)
const selectedUserId = ref(null)
const userOptions = ref([])
const groupUsers = ref([])
const selectedPermissions = ref([])

const form = ref({
  groupId: null,
  groupName: '',
  description: ''
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户组' : '新增用户组')

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await request.get('/group/list')
    if (res.code === 200) {
      groupList.value = res.data
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = { groupId: null, groupName: '', description: '' }
  dialogVisible.value = true
}

const editGroup = (row) => {
  isEdit.value = true
  form.value = {
    groupId: row.groupId,
    groupName: row.groupName,
    description: row.description
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.groupName) {
    ElMessage.warning('请输入组名称')
    return
  }
  try {
    if (isEdit.value) {
      await request.post('/group/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/group/save', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadGroups()
  } catch (e) {
    // handled by interceptor
  }
}

const deleteGroup = (row) => {
  ElMessageBox.confirm('确认删除该用户组吗？将同时删除该组的所有成员和权限关联。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/group/delete', { groupId: row.groupId })
      ElMessage.success('删除成功')
      loadGroups()
    } catch (e) {
      // handled
    }
  }).catch(() => {})
}

const showDetail = async (row) => {
  currentGroup.value = row
  activeTab.value = 'members'
  detailVisible.value = true
  await loadAllUsers()
  await loadGroupUsers()
  await loadGroupPermissions()
}

const loadAllUsers = async () => {
  try {
    const res = await request.get('/user/all')
    if (res.code === 200) {
      userOptions.value = res.data || []
    }
  } catch (e) {
    // handled
  }
}

const loadGroupUsers = async () => {
  memberLoading.value = true
  try {
    const res = await request.get('/group/users', { params: { groupId: currentGroup.value.groupId } })
    if (res.code === 200) {
      groupUsers.value = res.data
    }
  } catch (e) {
    // handled
  } finally {
    memberLoading.value = false
  }
}

const loadGroupPermissions = async () => {
  try {
    const res = await request.get('/group/permissions', { params: { groupId: currentGroup.value.groupId } })
    if (res.code === 200) {
      selectedPermissions.value = res.data || []
    }
  } catch (e) {
    // handled
  }
}

const addUserToGroup = async () => {
  if (!selectedUserId.value) return
  try {
    await request.post('/group/add-user', {
      groupId: currentGroup.value.groupId,
      userId: selectedUserId.value
    })
    ElMessage.success('添加成功')
    selectedUserId.value = null
    userOptions.value = []
    loadGroupUsers()
    loadGroups()
  } catch (e) {
    // handled
  }
}

const removeUser = (row) => {
  ElMessageBox.confirm('确认移除该用户？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/group/remove-user', {
        groupId: currentGroup.value.groupId,
        userId: row.userId
      })
      ElMessage.success('移除成功')
      loadGroupUsers()
      loadGroups()
    } catch (e) {
      // handled
    }
  }).catch(() => {})
}

const savePermissions = async () => {
  try {
    await request.post('/group/set-permissions', {
      groupId: currentGroup.value.groupId,
      permissions: selectedPermissions.value
    })
    ElMessage.success('权限保存成功')
    loadGroups()
  } catch (e) {
    // handled
  }
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
</style>
