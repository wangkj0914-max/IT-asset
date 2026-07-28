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
          <template #header><span>资产分类分布</span></template>
          <div ref="categoryChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>部门资产分布 (Top 10)</span></template>
          <div ref="deptChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 耗材消耗趋势 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>耗材消耗趋势（近6个月）</span></template>
          <div ref="trendChartRef" style="height:300px;padding:10px 20px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 低库存提醒 -->
    <el-card shadow="hover" style="margin-top:16px" v-if="lowStockItems && lowStockItems.length > 0">
      <template #header><span style="color:#E6A23C">⚠ 低库存耗材预警</span></template>
      <el-table :data="lowStockItems" size="small" stripe>
        <el-table-column prop="consumableName" label="名称" />
        <el-table-column label="当前库存" width="120"><template #default="{row}"><b style="color:#F56C6C">{{row.currentStock}} {{row.unit}}</b></template></el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100"/>
        <el-table-column label="建议补充量" width="110">
          <template #default="{row}"><el-tag type="warning">{{row.suggestedReplenishment || (row.minStock - row.currentStock)}} {{row.unit}}</el-tag></template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100"/>
      </el-table>
    </el-card>

    <el-row :gutter="16" style="margin-top:16px" v-if="maintenanceItems && maintenanceItems.length > 0">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="color:#E6A23C">🔧 30天内维护到期</span></template>
          <el-table :data="maintenanceItems" size="small" stripe>
            <el-table-column prop="assetCode" label="资产编号" width="140"/>
            <el-table-column prop="assetName" label="名称" />
            <el-table-column prop="department" label="部门" width="120"/>
            <el-table-column label="下次维护" width="110"><template #default="{row}">{{row.nextMaintenanceDate}}</template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px" v-if="licenseExpiring && licenseExpiring.length > 0">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="color:#F56C6C">📋 许可证即将到期</span></template>
          <el-table :data="licenseExpiring" size="small" stripe>
            <el-table-column prop="softwareName" label="软件名称" />
            <el-table-column prop="vendor" label="厂商" width="100"/>
            <el-table-column prop="usedCount" label="已用" width="60"/>
            <el-table-column prop="totalCount" label="总数" width="60"/>
            <el-table-column label="到期" width="110"><template #default="{row}"><b style="color:#F56C6C">{{row.expireDate}}</b></template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 空状态引导 -->
  <el-card shadow="hover" v-if="statCards[0].value === 0" style="margin:16px 20px;text-align:center;padding:40px">
    <el-empty description="当前站点暂无资产数据">
      <el-button type="primary" @click="router.push('/asset-manage')">开始录入资产</el-button>
    </el-empty>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Monitor, Box, DataLine, Timer, Bell, Coin, Money, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '@/utils/request'

const router = useRouter()
const categoryChartRef = ref(null), deptChartRef = ref(null), trendChartRef = ref(null)
const lowStockItems = ref([])
const maintenanceItems = ref([])
const licenseExpiring = ref([])

const statCards = reactive([
  { label:'资产总数', value:0, icon:Monitor, color:'#409EFF' },
  { label:'未领用', value:0, icon:Box, color:'#67C23A' },
  { label:'已领用', value:0, icon:DataLine, color:'#E6A23C' },
  { label:'待审批', value:0, icon:Bell, color:'#9C27B0' },
  { label:'低库存耗材', value:0, icon:Timer, color:'#F56C6C' },
  { label:'资产总值', value:'¥0', icon:Coin, color:'#FF9800' },
  { label:'折旧概览', value:'加载中', icon:Money, color:'#67C23A' },
  { label:'逾期资产', value:0, icon:Warning, color:'#F56C6C' },
])

const loadData = async () => {
  try {
    const r = await request.get('/home/dashboard')
    if (r.code !== 200) return
    const d = r.data
    statCards[0].value = d.totalAssets || 0
    statCards[1].value = d.unusedAssets || 0
    statCards[2].value = d.usedAssets || 0
    statCards[3].value = d.pendingApprovalCount || 0
    statCards[4].value = d.lowStockCount || 0
    statCards[5].value = '¥' + ((d.totalAssetValue ? Number(d.totalAssetValue).toLocaleString('zh-CN') : 0))
    // 折旧概览卡
    if (d.depreciationSummary) {
      const ds = d.depreciationSummary
      statCards[6].value = '¥' + (ds.totalCurrentValue ? Number(ds.totalCurrentValue).toLocaleString('zh-CN') : 0) + ' / ¥' + (ds.totalOriginalValue ? Number(ds.totalOriginalValue).toLocaleString('zh-CN') : 0)
      statCards[6].label = '折旧率 ' + (ds.depreciationRate || 0) + '%'
    }
    // 逾期资产卡
    statCards[7].value = d.overdueAssetCount || 0

    lowStockItems.value = d.lowStockItems || []
    maintenanceItems.value = d.maintenanceItems || []
    licenseExpiring.value = d.licenseExpiringItems || []

    await nextTick()
    renderCharts(d.categoryDistribution || [], d.departmentDistribution || [], d.monthlyConsumption || [])
  } catch (e) {
    ElMessage.error('加载首页数据失败')
  }
}

// 加载低库存告警（使用 /consumable/alerts）
const loadAlerts = async () => {
  try {
    const r = await request.get('/consumable/alerts')
    if (r.code === 200 && r.data) {
      lowStockItems.value = r.data
      statCards[4].value = r.data.length
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
      yAxis:{type:'value',name:'出库数量'},
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
