<template>
  <div class="approval-center">
    <div class="page-header">
      <h2>审批中心</h2>
    </div>

    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane name="all">
        <template #label>
          <span>全部待审批 <el-badge :value="totalPending" :hidden="totalPending === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="inbound">
        <template #label>
          <span>入库审批 <el-badge :value="pendingCounts.inbound" :hidden="pendingCounts.inbound === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="use">
        <template #label>
          <span>领用审批 <el-badge :value="pendingCounts.use" :hidden="pendingCounts.use === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="scrap">
        <template #label>
          <span>报废审批 <el-badge :value="pendingCounts.scrap" :hidden="pendingCounts.scrap === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="transfer">
        <template #label>
          <span>调拨审批 <el-badge :value="pendingCounts.transfer" :hidden="pendingCounts.transfer === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="return">
        <template #label>
          <span>归还审批 <el-badge :value="pendingCounts.return" :hidden="pendingCounts.return === 0" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="consumableUse">
        <template #label>
          <span>耗材领用 <el-badge :value="pendingCounts.consumableUse" :hidden="pendingCounts.consumableUse === 0" /></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <el-alert v-if="totalPending === 0 && !loading" title="暂无待审批事项" type="success" :closable="false" show-icon style="margin-top:20px" />

    <div v-loading="loading" style="margin-top:16px">
      <el-table :data="tableData" stripe v-if="tableData.length > 0">
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="tagType(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题/名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="applicant" label="申请人" width="120" />
        <el-table-column prop="department" label="部门" width="120" v-if="showDept" />
        <el-table-column label="申请时间" width="170" align="center">
          <template #default="{ row }">{{ row.time ? row.time.substring(0, 16).replace('T', ' ') : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.statusLabel || '待审批' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="approve(row, 1)">通过</el-button>
            <el-button type="danger" size="small" @click="approve(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('all')
const loading = ref(false)
const pendingData = ref({})
const totalPending = ref(0)
const pendingCounts = ref({ inbound: 0, use: 0, scrap: 0, transfer: 0, return: 0, consumableUse: 0 })
const showDept = ref(false)

const tableData = computed(() => {
  if (activeTab.value === 'all') {
    let all = []
    const keys = ['inbound', 'use', 'scrap', 'transfer', 'return', 'consumableUse']
    keys.forEach(k => {
      const section = pendingData.value[k]
      if (section && section.items) all = all.concat(section.items)
    })
    return all
  }
  const section = pendingData.value[activeTab.value]
  return section && section.items ? section.items : []
})

const tagType = (type) => {
  const map = { '入库': 'primary', '领用': 'success', '报废': 'danger', '调拨': 'warning', '归还': 'info', '耗材领用': '' }
  return map[type] || 'info'
}

const loadPending = async () => {
  loading.value = true
  try {
    const res = await request.get('/approval/pending')
    if (res.code === 200) {
      pendingData.value = res.data
      pendingCounts.value.inbound = res.data.inbound?.count || 0
      pendingCounts.value.use = res.data.use?.count || 0
      pendingCounts.value.scrap = res.data.scrap?.count || 0
      pendingCounts.value.transfer = res.data.transfer?.count || 0
      pendingCounts.value.return = res.data.return?.count || 0
      pendingCounts.value.consumableUse = res.data.consumableUse?.count || 0
      totalPending.value = res.data.totalPending?.count || 0
    }
  } catch (e) {
    ElMessage.error('加载失败')
  }
  loading.value = false
}

const handleTabClick = (tab) => {
  showDept.value = tab.props.name === 'use'
}

const getApproveUrl = (type) => {
  const map = { '入库': '/inbound/approve', '领用': '/use/approve', '报废': '/scrap/approve', '调拨': '/transfer/approve' }
  return map[type] || ''
}

const approve = async (row, approveStatus) => {
  const action = approveStatus === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}该项申请？`, '审批确认', { type: 'warning' })
    const configs = {
      '入库': { url: '/inbound/audit', idField: 'inboundId', valField: 'approved', val: approveStatus === 1 },
      '领用': { url: '/use/approve',   idField: 'recordId',  valField: 'approved', val: approveStatus === 1 },
      '报废': { url: '/scrap/approve', idField: 'scrapId',   valField: 'approved', val: approveStatus === 1 },
      '调拨': { url: '/transfer/approve', idField: 'transferId', valField: 'status', val: approveStatus },
      '归还': { url: '/return/approve', idField: 'returnId', valField: 'approved', val: approveStatus === 1 },
      '耗材领用': { url: '/consumable-use/approve', idField: 'recordId', valField: 'approved', val: approveStatus === 1 }
    }
    const c = configs[row.type]
    if (!c) return ElMessage.error('未知类型')
    const res = await request.post(c.url, null, { params: { [c.idField]: row.id, [c.valField]: c.val } })
    if (res.code === 200) {
      ElMessage.success(`已${action}`)
      loadPending()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) { /* cancelled */ }
}

onMounted(loadPending)
</script>

<style scoped>
.approval-center { padding: 16px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
