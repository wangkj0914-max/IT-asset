<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" v-for="(card, i) in statCards" :key="i">
        <el-card shadow="hover" :body-style="{padding:'20px',height:'100%'}"
          class="stat-card-item"
          @click="i === 7 ? router.push('/asset-use') : undefined"
          :style="i === 7 ? {cursor: 'pointer'} : {}">
          <div class="card-inner">
            <div class="card-icon" :style="{background:card.color}"><el-icon :size="28"><component :is="card.icon"/></el-icon></div>
            <div class="card-info">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>{{ t('home.categoryDist') }}</span></template>
          <div ref="categoryChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>{{ t('home.deptDist') }}</span></template>
          <div ref="deptChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 耗材消耗趋势 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>{{ t('home.consumptionTrend') }}</span></template>
          <div ref="trendChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 低库存提醒 -->
    <el-card shadow="hover" style="margin-top:16px" v-if="lowStockItems && lowStockItems.length > 0">
      <template #header><span style="color:#E6A23C">⚠ {{ t('home.lowStockAlert') }}</span></template>
      <el-table :data="lowStockItems" size="small" stripe>
        <el-table-column prop="consumableName" :label="t('home.name')" />
        <el-table-column :label="t('home.currentStock')" width="120"><template #default="{row}"><b style="color:#F56C6C">{{row.currentStock}} {{row.unit}}</b></template></el-table-column>
        <el-table-column prop="minStock" :label="t('home.minStock')" width="100"/>
        <el-table-column :label="t('home.suggestedReplenishment')" width="110">
          <template #default="{row}"><el-tag type="warning">{{row.suggestedReplenishment || (row.minStock - row.currentStock)}} {{row.unit}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="category" :label="t('home.category')" width="100"/>
      </el-table>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px" v-if="maintenanceItems && maintenanceItems.length > 0">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="color:#E6A23C">🔧 {{ t('home.maintenanceDue') }}</span></template>
          <el-table :data="maintenanceItems" size="small" stripe>
            <el-table-column prop="assetCode" :label="t('home.assetCode')" width="140"/>
            <el-table-column prop="assetName" :label="t('home.name')" />
            <el-table-column prop="department" :label="t('home.department')" width="120"/>
            <el-table-column :label="t('home.nextMaintenance')" width="110"><template #default="{row}">{{row.nextMaintenanceDate}}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px" v-if="licenseExpiring && licenseExpiring.length > 0">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="color:#F56C6C">📋 {{ t('home.licenseExpiring') }}</span></template>
          <el-table :data="licenseExpiring" size="small" stripe>
            <el-table-column prop="softwareName" :label="t('home.softwareName')" />
            <el-table-column prop="vendor" :label="t('home.vendor')" width="100"/>
            <el-table-column prop="usedCount" :label="t('home.usedQty')" width="60"/>
            <el-table-column prop="totalCount" :label="t('home.totalQty')" width="60"/>
            <el-table-column :label="t('home.expire')" width="110"><template #default="{row}"><b style="color:#F56C6C">{{row.expireDate}}</b></template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 空状态引导 -->
  <el-card shadow="hover" v-if="counts.total === 0" style="margin:16px 20px;text-align:center;padding:40px">
    <el-empty :description="t('home.emptyTip')">
      <el-button type="primary" @click="router.push('/asset-manage')">{{ t('home.startEnter') }}</el-button>
    </el-empty>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { Monitor, Box, DataLine, Timer, Bell, Coin, Money, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'

const { t } = useI18n()
const router = useRouter()
const categoryChartRef = ref(null), deptChartRef = ref(null), trendChartRef = ref(null)
const lowStockItems = ref([])
const maintenanceItems = ref([])
const licenseExpiring = ref([])

// 原始计数（与语言无关），statCards 由它 + t() 派生，切换语言可即时更新
const counts = reactive({
  total: 0, unused: 0, used: 0, pending: 0, lowStock: 0,
  totalValue: '¥0', depValue: '', depRate: 0, overdue: 0
})

const statCards = computed(() => [
  { label: t('home.totalAssets'), value: counts.total, icon: Monitor, color: '#409EFF' },
  { label: t('home.unused'), value: counts.unused, icon: Box, color: '#67C23A' },
  { label: t('home.used'), value: counts.used, icon: DataLine, color: '#E6A23C' },
  { label: t('home.pending'), value: counts.pending, icon: Bell, color: '#9C27B0' },
  { label: t('home.lowStock'), value: counts.lowStock, icon: Timer, color: '#F56C6C' },
  { label: t('home.totalValue'), value: counts.totalValue, icon: Coin, color: '#FF9800' },
  { label: t('home.depRate') + ' ' + counts.depRate + '%', value: counts.depValue, icon: Money, color: '#67C23A' },
  { label: t('home.overdue'), value: counts.overdue, icon: Warning, color: '#F56C6C' }
])

const loadData = async () => {
  try {
    const r = await request.get('/home/dashboard')
    if (r.code !== 200) return
    const d = r.data
    counts.total = d.totalAssets || 0
    counts.unused = d.unusedAssets || 0
    counts.used = d.usedAssets || 0
    counts.pending = d.pendingApprovalCount || 0
    counts.lowStock = d.lowStockCount || 0
    counts.totalValue = '¥' + ((d.totalAssetValue ? Number(d.totalAssetValue).toLocaleString('zh-CN') : 0))
    // 折旧概览卡
    if (d.depreciationSummary) {
      const ds = d.depreciationSummary
      counts.depValue = '¥' + (ds.totalCurrentValue ? Number(ds.totalCurrentValue).toLocaleString('zh-CN') : 0) + ' / ¥' + (ds.totalOriginalValue ? Number(ds.totalOriginalValue).toLocaleString('zh-CN') : 0)
      counts.depRate = ds.depreciationRate || 0
    }
    // 逾期资产卡
    counts.overdue = d.overdueAssetCount || 0

    lowStockItems.value = d.lowStockItems || []
    maintenanceItems.value = d.maintenanceItems || []
    licenseExpiring.value = d.licenseExpiringItems || []

    await nextTick()
    renderCharts(d.categoryDistribution || [], d.departmentDistribution || [], d.monthlyConsumption || [])
  } catch (e) {
    ElMessage.error(t('home.loadFailed'))
  }
}

// 加载低库存告警（使用 /consumable/alerts）
const loadAlerts = async () => {
  try {
    const r = await request.get('/consumable/alerts')
    if (r.code === 200 && r.data) {
      lowStockItems.value = r.data
      counts.lowStock = r.data.length
    }
  } catch (e) { /* ignore */ }
}

const renderCharts = (catDist, deptDist, monthlyConsumption) => {
  if (categoryChartRef.value) {
    const c = echarts.init(categoryChartRef.value)
    c.setOption({
      tooltip:{trigger:'item'},
      series:[{type:'pie',radius:['45%','70%'],data:catDist,label:{formatter:'{b}: {c}'}}]
    })
  }
  if (deptChartRef.value) {
    const c = echarts.init(deptChartRef.value)
    c.setOption({
      tooltip:{trigger:'axis'},
      xAxis:{type:'category',data:deptDist.map(d=>d.name),axisLabel:{rotate:30}},
      yAxis:{type:'value'},
      series:[{type:'bar',data:deptDist.map(d=>d.value),itemStyle:{color:'#409EFF'},barMaxWidth:30}]
    })
  }
  if (trendChartRef.value && monthlyConsumption && monthlyConsumption.length > 0) {
    const c = echarts.init(trendChartRef.value)
    c.setOption({
      tooltip:{trigger:'axis'},
      xAxis:{type:'category',data:monthlyConsumption.map(d=>d.month)},
      yAxis:{type:'value',name:t('home.outboundQty')},
      series:[{type:'line',data:monthlyConsumption.map(d=>d.totalQuantity),smooth:true,itemStyle:{color:'#67C23A'},areaStyle:{color:'rgba(103,194,58,0.15)'}}]
    })
  }
}

onMounted(() => { loadData(); loadAlerts() })
</script>

<style scoped>
.dashboard{padding:0}
.stat-cards{margin-bottom:16px;gap:0}
.stat-card-item{height:100%}
.stat-cards .el-col{margin-bottom:16px}
.card-inner{display:flex;align-items:center;gap:16px}
.card-icon{width:56px;height:56px;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#fff;flex-shrink:0}
.card-value{font-size:24px;font-weight:bold;color:#303133}
.card-label{font-size:13px;color:#909399;margin-top:4px}
</style>
