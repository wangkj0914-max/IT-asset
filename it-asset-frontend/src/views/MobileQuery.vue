<template>
  <div class="mobile-page">
    <div class="mobile-header">
      <span>资产查询</span>
      <span class="site-tag">{{ currentSite }}</span>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="输入资产编号或名称" clearable @keyup.enter="search" @clear="search">
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #append>
          <el-button @click="search" :loading="loading">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading"><Loading /></el-icon> 查询中...
    </div>

    <div v-if="!loading && searched && results.length === 0" class="empty-box">
      <el-empty description="未找到匹配资产" />
    </div>

    <div v-if="results.length > 0" class="result-list">
      <div v-for="item in results" :key="item.assetId" class="asset-card" @click="showDetail(item)">
        <div class="card-row">
          <span class="asset-code">{{ item.assetCode }}</span>
          <el-tag :type="item.status === 0 ? 'success' : item.status === 1 ? '' : 'danger'" size="small">
            {{ statusText(item.status) }}
          </el-tag>
        </div>
        <div class="card-name">{{ item.assetName }}</div>
        <div class="card-meta">
          <span>{{ item.brand }} {{ item.model }}</span>
          <span>{{ item.storageLocation || '-' }}</span>
        </div>
        <div class="card-user" v-if="item.userName">
          使用人: {{ item.userName }}
        </div>
      </div>
      <div class="pagination-row">
        <el-pagination small layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="current" @current-change="search" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="资产详情" width="92%">
      <template v-if="detail">
        <div class="detail-item"><label>编号</label><span>{{ detail.assetCode }}</span></div>
        <div class="detail-item"><label>名称</label><span>{{ detail.assetName }}</span></div>
        <div class="detail-item"><label>品牌型号</label><span>{{ detail.brand }} {{ detail.model }}</span></div>
        <div class="detail-item"><label>状态</label><span><el-tag size="small">{{ statusText(detail.status) }}</el-tag></span></div>
        <div class="detail-item"><label>存放位置</label><span>{{ detail.storageLocation || '-' }}</span></div>
        <div class="detail-item"><label>使用人</label><span>{{ detail.userName || '-' }}</span></div>
        <div class="detail-item"><label>部门</label><span>{{ detail.department || '-' }}</span></div>
        <div class="detail-item"><label>序列号</label><span>{{ detail.serialNumber || '-' }}</span></div>
        <div class="detail-item" v-if="detail.purchaseCost"><label>采购金额</label><span>¥{{ detail.purchaseCost }}</span></div>
        <div class="detail-item" v-if="detail.currentValue"><label>当前价值</label><span>¥{{ detail.currentValue }}</span></div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Loading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(15)
const currentSite = ref(localStorage.getItem('site') || '')

const detailVisible = ref(false)
const detail = ref(null)

const statusText = (s) => {
  const map = { 0: '在库', 1: '已领用', 2: '维修中', 3: '已报废', 4: '可部署' }
  return map[s] || '未知'
}

const search = async () => {
  loading.value = true
  searched.value = true
  try {
    const res = await request.get('/assetInfo/page', {
      params: { current: current.value, size: size.value, keyword: keyword.value || undefined }
    })
    results.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {
    results.value = []
  }
  loading.value = false
}

const showDetail = (item) => {
  detail.value = item
  detailVisible.value = true
}

onMounted(() => {
  if (keyword.value) search()
})
</script>

<style scoped>
.mobile-page {
  max-width: 500px;
  margin: 0 auto;
  padding: 16px;
  min-height: 100vh;
  background: #F8FAFC;
}
.mobile-header {
  font-size: 20px;
  font-weight: bold;
  color: #1A1A2E;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.site-tag { font-size: 12px; color: #64748B; }
.search-bar { margin-bottom: 16px; }
.loading-box, .empty-box { text-align: center; padding: 40px 0; color: #64748B; }
.asset-card {
  background: white;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  cursor: pointer;
}
.card-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.asset-code { font-size: 12px; color: #64748B; font-family: monospace; }
.card-name { font-size: 16px; font-weight: 600; color: #1A1A2E; margin-bottom: 6px; }
.card-meta { font-size: 12px; color: #94A3B8; display: flex; gap: 16px; }
.card-user { font-size: 12px; color: #028090; margin-top: 4px; }
.pagination-row { display: flex; justify-content: center; margin-top: 12px; }
.detail-item { display: flex; padding: 10px 0; border-bottom: 1px solid #F1F5F9; }
.detail-item label { width: 80px; color: #64748B; font-size: 13px; flex-shrink: 0; }
.detail-item span { font-size: 13px; color: #1A1A2E; }
</style>
