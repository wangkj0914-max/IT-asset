<template>
  <div id="app">
    <!-- 登录页不显示布局 -->
    <template v-if="isLoginPage">
      <router-view />
    </template>
    
    <!-- 主布局 -->
    <el-container v-else class="main-layout">
      <!-- 左侧导航栏 -->
      <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="logo" v-if="!sidebarCollapsed">
          <span>IT 资产管理系统</span>
        </div>
        <div class="logo logo-mini" v-else>
          <span>IT</span>
        </div>

        <!-- 收缩按钮 -->
        <div class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon :size="18">
            <Fold v-if="!sidebarCollapsed" /><Expand v-else />
          </el-icon>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :router="false"
          :collapse="sidebarCollapsed"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <!-- 系统首页 -->
          <el-menu-item index="/home" @click="navigateTo('/home')">
            <el-icon><House /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <!-- 信息管理 -->
          <el-sub-menu index="info">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span>信息管理</span>
            </template>
            <el-menu-item index="/notice" @click="navigateTo('/notice')">
              <el-icon><Notification /></el-icon>
              <span>公告信息</span>
            </el-menu-item>
            <el-menu-item index="/department-info" @click="navigateTo('/department-info')">
              <el-icon><View /></el-icon>
              <span>部门信息</span>
            </el-menu-item>
            <el-menu-item index="/storage-location" @click="navigateTo('/storage-location')">
              <el-icon><Location /></el-icon>
              <span>存放地点</span>
            </el-menu-item>
            <el-menu-item index="/consumable-manage" @click="navigateTo('/consumable-manage')">
              <el-icon><Box /></el-icon>
              <span>耗材管理</span>
            </el-menu-item>
            <el-menu-item index="/license-manage" @click="navigateTo('/license-manage')">
              <el-icon><Key /></el-icon>
              <span>许可证管理</span>
            </el-menu-item>
            <el-menu-item index="/user-manage" @click="navigateTo('/user-manage')">
              <el-icon><User /></el-icon>
              <span>员工信息</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 审批中心 -->
          <el-menu-item index="/approval-center" @click="navigateTo('/approval-center')" v-if="userRole === 2">
            <el-icon><Check /></el-icon>
            <span>审批中心</span>
            <el-badge :value="pendingTotal" :hidden="pendingTotal === 0" class="approval-badge" />
          </el-menu-item>

          <!-- 资产管理 -->
          <el-sub-menu index="asset">
            <template #title>
              <el-icon><Briefcase /></el-icon>
              <span>资产管理</span>
            </template>
            <el-menu-item index="/category-manage" @click="navigateTo('/category-manage')">
              <el-icon><List /></el-icon>
              <span>资产分类</span>
            </el-menu-item>
            <el-menu-item index="/asset-manage" @click="navigateTo('/asset-manage')">
              <el-icon><Document /></el-icon>
              <span>固定资产</span>
            </el-menu-item>
            <el-menu-item index="/asset-inbound" @click="navigateTo('/asset-inbound')">
              <el-icon><Plus /></el-icon>
              <span>资产入库</span>
            </el-menu-item>
            <el-menu-item index="/asset-use" @click="navigateTo('/asset-use')">
              <el-icon><User /></el-icon>
              <span>资产领用</span>
            </el-menu-item>
            <el-menu-item index="/asset-return" @click="navigateTo('/asset-return')">
              <el-icon><Refresh /></el-icon>
              <span>归还记录</span>
            </el-menu-item>
            <el-menu-item index="/asset-repair" @click="navigateTo('/asset-repair')">
              <el-icon><Tools /></el-icon>
              <span>资产维修</span>
            </el-menu-item>
            <el-menu-item index="/asset-scrap" @click="navigateTo('/asset-scrap')">
              <el-icon><Delete /></el-icon>
              <span>资产报废</span>
            </el-menu-item>
            <el-menu-item index="/asset-inventory" @click="navigateTo('/asset-inventory')">
              <el-icon><Search /></el-icon>
              <span>资产盘点</span>
            </el-menu-item>
            <el-menu-item index="/asset-transfer" @click="navigateTo('/asset-transfer')">
              <el-icon><Switch /></el-icon>
              <span>资产调拨</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 系统管理（仅管理员） -->
          <el-sub-menu index="system" v-if="userRole === 2">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/workflow-settings" @click="navigateTo('/workflow-settings')">
              <el-icon><Setting /></el-icon>
              <span>流程设置</span>
            </el-menu-item>
            <el-menu-item index="/integration" @click="navigateTo('/integration')">
              <el-icon><Document /></el-icon>
              <span>集成配置</span>
            </el-menu-item>
            <el-menu-item index="/api-manage" @click="navigateTo('/api-manage')">
              <el-icon><DocumentAdd /></el-icon>
              <span>接口管理</span>
            </el-menu-item>
            <el-menu-item index="/operation-log" @click="navigateTo('/operation-log')">
              <el-icon><List /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container class="main-container">
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/home' }">系统首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-select v-model="currentSite" @change="switchSite" style="width:120px;margin-right:16px">
              <el-option label="苏州" value="苏州" />
              <el-option label="Penang" value="Penang" />
            </el-select>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar :size="32" :icon="UserFilled" style="margin-right: 8px;" />
                {{ realName }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="changePwd">修改密码</el-dropdown-item>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 内容区域 -->
        <el-main class="content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '@/utils/request'
import { 
  House, User, UserFilled, Document, List, 
  Tools, Setting, MoreFilled, ArrowDown,
  DocumentAdd, Grid, Notification, View, Briefcase,
  Plus, Search, Switch, Delete, Fold, Expand, Location, Box, Key
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const username = ref(localStorage.getItem('username') || '用户')
const realName = ref(localStorage.getItem('realName') || localStorage.getItem('username') || '用户')
const userRole = computed(() => parseInt(localStorage.getItem('role') || '1'))
const currentTitle = ref('')
const sidebarCollapsed = ref(false)
const currentSite = ref(localStorage.getItem('site') || '苏州')
const pendingTotal = ref(0)
let pendingTimer = null

const switchSite = (val) => {
  localStorage.setItem('site', val)
  window.location.reload()
}

const isLoginPage = computed(() => route.path === '/')

const activeMenu = computed(() => {
  const pathMap = {
    '/home': '/home',
    '/profile': '/profile',
    '/user-manage': '/user-manage',
    '/category-manage': '/category-manage',
    '/asset-manage': '/asset-manage',
    '/asset-use': '/asset-use',
    '/asset-return': '/asset-return',
    '/asset-repair': '/asset-repair',
    '/asset-inbound': '/asset-inbound',
    '/asset-inventory': '/asset-inventory',
    '/asset-scrap': '/asset-scrap',
    '/notice': '/notice',
    '/department-info': '/department-info',
    '/storage-location': '/storage-location',
    '/consumable-manage': '/consumable-manage',
    '/license-manage': '/license-manage',
    '/change-password': '/change-password',
    '/asset-transfer': '/asset-transfer',
    '/approval-center': '/approval-center',
    '/integration': '/integration',
    '/api-manage': '/api-manage',
    '/operation-log': '/operation-log'
  }
  return pathMap[route.path] || '/home'
})

const roleText = computed(() => {
  return userRole.value === 2 ? '管理员' : '普通用户'
})

const navigateTo = (path) => {
  router.push(path)
  updateTitle(path)
}

const updateTitle = (path) => {
  const titleMap = {
    '/home': '系统首页',
    '/profile': '个人资产',
    '/user-manage': '员工信息',
    '/category-manage': '资产分类',
    '/asset-manage': '固定资产',
    '/asset-use': '资产领用',
    '/asset-return': '归还记录',
    '/asset-repair': '资产维修',
    '/asset-inbound': '资产入库',
    '/asset-inventory': '资产盘点',
    '/asset-scrap': '资产报废',
    '/notice': '公告信息',
    '/department-info': '部门信息',
    '/storage-location': '存放地点',
    '/consumable-manage': '耗材管理',
    '/license-manage': '许可证管理',
    '/asset-transfer': '资产调拨',
    '/approval-center': '审批中心',
    '/integration': '集成配置',
    '/api-manage': '接口管理',
    '/operation-log': '操作日志'
  }
  currentTitle.value = titleMap[path] || ''
}

const handleMenuSelect = (index) => {
  // Menu selection handled by navigateTo in each @click
}

const handleCommand = async (command) => {
  if (command === 'changePwd') {
    router.push('/change-password')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确认要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('role')
      ElMessage.success('已退出登录')
      router.push('/')
    } catch {
      // 取消退出
    }
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  updateTitle(route.path)
  
  // 检查登录状态
  if (!isLoginPage.value && !localStorage.getItem('token')) {
    router.push('/')
  }

  // 定期拉取待审批数
  const fetchPending = async () => {
    try {
      const r = await request.get('/approval/pending')
      if (r.code === 200) pendingTotal.value = r.data?.totalPending?.count || 0
    } catch (e) { /* 网络异常静默忽略 */ }
  }
  if (userRole.value === 2) {
    fetchPending()
    pendingTimer = setInterval(fetchPending, 30000)
  }
})

onUnmounted(() => {
  if (pendingTimer) clearInterval(pendingTimer)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  min-height: 100vh;
}

/* 主布局 */
.main-layout {
  height: 100vh;
}

/* 左侧导航栏 */
.sidebar {
  background-color: #304156;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
  overflow: hidden;
  white-space: nowrap;
}

.logo-mini {
  font-size: 18px;
}

.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
  color: #bfcbd9;
  cursor: pointer;
  border-top: 1px solid #3a4a5b;
  border-bottom: 1px solid #3a4a5b;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #409EFF;
  background-color: #263445;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  border-right: none;
}

.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #4a5568;
  border-radius: 3px;
}

/* 底部用户信息 */
.user-info {
  height: 60px;
  background-color: #2b3a4b;
  display: flex;
  align-items: center;
  padding: 0 15px;
  gap: 10px;
}

.user-detail {
  flex: 1;
  overflow: hidden;
}

.username {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-text {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.more-btn {
  color: #bfcbd9;
  cursor: pointer;
  font-size: 18px;
  padding: 5px;
}

.more-btn:hover {
  color: #fff;
}

/* 主内容区 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* 顶部导航栏 */
.header {
  height: 50px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.user-dropdown:hover {
  color: #409EFF;
}

/* 内容区域 */
.content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.content::-webkit-scrollbar {
  width: 8px;
}

.content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

/* 菜单项样式 */
.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background-color: #263445 !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #fff !important;
}
@media (max-width: 768px) {
  .sidebar { width: 0 !important; overflow: hidden; }
  .sidebar:not(.collapsed) { width: 180px !important; }
  .header-select { width: 80px !important; }
}
</style>
