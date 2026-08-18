import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import AssetManage from '@/views/AssetManage.vue'
import AssetUse from '@/views/AssetUse.vue'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    meta: { title: '系统登录' }
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: '首页', requireAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人信息', requireAuth: true }
  },
  {
    path: '/user-manage',
    name: 'UserManage',
    component: () => import('@/views/UserManage.vue'),
    meta: { title: '用户管理', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { title: '修改密码', requireAuth: true }
  },
  {
    path: '/category-manage',
    name: 'CategoryManage',
    component: () => import('@/views/CategoryManage.vue'),
    meta: { title: '分类管理', requireAuth: true }
  },
  {
    path: '/model-manage',
    name: 'ModelManage',
    component: () => import('@/views/ModelManage.vue'),
    meta: { title: '资产模型', requireAuth: true }
  },
  {
    path: '/asset-manage',
    name: 'AssetManage',
    component: AssetManage,
    meta: { title: '资产信息管理', requireAuth: true }
  },
  {
    path: '/asset-use',
    name: 'AssetUse',
    component: AssetUse,
    meta: { title: '领用申请', requireAuth: true }
  },
  {
    path: '/approval-center',
    name: 'ApprovalCenter',
    component: () => import('@/views/ApprovalCenter.vue'),
    meta: { title: '审批中心', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/workflow-settings',
    name: 'WorkflowSettings',
    component: () => import('@/views/WorkflowSettings.vue'),
    meta: { title: '流程设置', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/asset-return',
    name: 'AssetReturn',
    component: () => import('@/views/AssetReturn.vue'),
    meta: { title: '资产归还管理', requireAuth: true }
  },
  {
    path: '/asset-repair',
    name: 'AssetRepair',
    component: () => import('@/views/AssetRepair.vue'),
    meta: { title: '资产维修管理', requireAuth: true }
  },
  {
    path: '/asset-inbound',
    name: 'AssetInbound',
    component: () => import('@/views/AssetInbound.vue'),
    meta: { title: '资产入库管理', requireAuth: true }
  },
  {
    path: '/asset-scrap',
    name: 'AssetScrap',
    component: () => import('@/views/AssetScrap.vue'),
    meta: { title: '资产报废管理', requireAuth: true }
  },
  {
    path: '/asset-inventory',
    name: 'AssetInventory',
    component: () => import('@/views/AssetInventory.vue'),
    meta: { title: '资产盘点管理', requireAuth: true }
  },
  {
    path: '/mobile-scan',
    name: 'MobileScan',
    component: () => import('@/views/MobileScan.vue'),
    meta: { title: '扫码盘点', requireAuth: true, layout: 'blank' }
  },
  {
    path: '/mobile-query',
    name: 'MobileQuery',
    component: () => import('@/views/MobileQuery.vue'),
    meta: { title: '资产查询', requireAuth: true, layout: 'blank' }
  },
  {
    path: '/mobile',
    name: 'MobileHome',
    component: () => import('@/views/MobileHome.vue'),
    meta: { title: '移动端', requireAuth: true, layout: 'blank' }
  },
  {
    path: '/notice',
    name: 'Notice',
    component: () => import('@/views/Notice.vue'),
    meta: { title: '公告信息', requireAuth: true }
  },
  {
    path: '/storage-location',
    name: 'StorageLocation',
    component: () => import('@/views/StorageLocation.vue'),
    meta: { title: '存放地点', requireAuth: true }
  },
  {
    path: '/custom-field-manage',
    name: 'CustomFieldManage',
    component: () => import('@/views/CustomFieldManage.vue'),
    meta: { title: '自定义字段', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/consumable-manage',
    name: 'ConsumableManage',
    component: () => import('@/views/ConsumableManage.vue'),
    meta: { title: '耗材管理', requireAuth: true }
  },
  {
    path: '/license-manage',
    name: 'LicenseManage',
    component: () => import('@/views/LicenseManage.vue'),
    meta: { title: '许可证管理', requireAuth: true }
  },
  {
    path: '/contract-manage',
    name: 'ContractManage',
    component: () => import('@/views/ContractManage.vue'),
    meta: { title: '合同管理', requireAuth: true }
  },
  {
    path: '/department-info',
    name: 'DepartmentInfo',
    component: () => import('@/views/DepartmentInfo.vue'),
    meta: { title: '部门信息', requireAuth: true }
  },
  {
    path: '/asset-transfer',
    name: 'AssetTransfer',
    component: () => import('@/views/AssetTransfer.vue'),
    meta: { title: '资产调拨', requireAuth: true }
  },
  {
    path: '/integration',
    name: 'Integration',
    component: () => import('@/views/Placeholder.vue'),
    meta: { title: '集成配置', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/api-manage',
    name: 'ApiManage',
    component: () => import('@/views/ApiManage.vue'),
    meta: { title: '接口管理', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/operation-log',
    name: 'OperationLog',
    component: () => import('@/views/OperationLog.vue'),
    meta: { title: '操作日志', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/Reports.vue'),
    meta: { title: '综合报表', requireAuth: true }
  },
  {
    path: '/group-manage',
    name: 'GroupManage',
    component: () => import('@/views/GroupManage.vue'),
    meta: { title: '用户组管理', requireAuth: true, requireAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Placeholder.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - IT 资产管理系统'
  }

  if (to.meta.requireAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录！')
      next('/')
      return
    }
    // 管理员路由额外检查
    if (to.meta.requireAdmin) {
      const role = parseInt(localStorage.getItem('role') || '1')
      if (role !== 2) {
        ElMessage.warning('仅管理员可访问此页面')
        next('/home')
        return
      }
    }
    next()
  } else {
    next()
  }
})

export default router
