<template>
  <div class="reports">
    <el-tabs v-model="activeTab" type="border-card" @tab-click="onTabClick">
      <!-- Tab 1: 资产生命周期 -->
      <el-tab-pane label="资产生命周期" name="lifecycle">
        <el-card shadow="hover" style="margin-bottom:16px">
          <template #header>
            <span>各分类资产状态分布 (共 {{ lifecycleData.totalAssets }} 件资产)</span>
          </template>
          <el-table :data="lifecycleData.byCategory" stripe size="small" max-height="400">
            <el-table-column prop="categoryName" label="资产分类" min-width="120" />
            <el-table-column prop="total" label="总数" width="80" sortable />
            <el-table-column prop="inStock" label="在库" width="80">
              <template #default="{row}"><el-tag type="info" size="small">{{row.inStock}}</el-tag></template>
            </el-table-column>
            <el-table-column prop="inUse" label="使用中" width="80">
              <template #default="{row}"><el-tag type="success" size="small">{{row.inUse}}</el-tag></template>
            </el-table-column>
            <el-table-column prop="repairing" label="维修中" width="80">
              <template #default="{row}"><el-tag type="warning" size="small">{{row.repairing}}</el-tag></template>
            </el-table-column>
            <el-table-column prop="scrapped" label="已报废" width="80">
              <template #default="{row}"><el-tag type="danger" size="small">{{row.scrapped}}</el-tag></template>
            </el-table-column>
            <el-table-column prop="avgAgeMonths" label="平均月龄" width="90" sortable />
          </el-table>
        </el-card>
        <el-card shadow="hover">
          <template #header><span>分类状态堆叠图</span></template>
          <div ref="lifecycleChartRef" class="chart-container" style="height:400px"></div>
        </el-card>
      </el-tab-pane>

      <!-- Tab 2: 盘点汇总 -->
      <el-tab-pane label="盘点汇总" name="inventory">
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="6">
            <el-card shadow="hover" :body-style="{padding:'20px',textAlign:'center'}">
              <div style="font-size:14px;color:#909399">综合准确率</div>
              <div style="font-size:36px;font-weight:bold;color:#67C23A;margin:8px 0">{{ inventoryData.overallAccuracy }}%</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" :body-style="{padding:'20px',textAlign:'center'}">
              <div style="font-size:14px;color:#909399">盘点次数</div>
              <div style="font-size:36px;font-weight:bold;color:#409EFF;margin:8px 0">{{ inventoryData.recentInventories?.length || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="hover" style="margin-bottom:16px">
          <template #header><span>盘点记录列表</span></template>
          <el-table :data="inventoryData.recentInventories" stripe size="small" max-height="350">
            <el-table-column prop="inventoryNo" label="盘点单号" width="160" />
            <el-table-column prop="inventoryName" label="盘点名称" min-width="150" />
            <el-table-column prop="date" label="盘点日期" width="120" />
            <el-table-column prop="totalChecked" label="盘点数" width="80" />
            <el-table-column prop="normalCount" label="正常" width="80">
              <template #default="{row}"><span style="color:#67C23A">{{row.normalCount}}</span></template>
            </el-table-column>
            <el-table-column prop="surplusCount" label="盘盈" width="80">
              <template #default="{row}"><span style="color:#409EFF">{{row.surplusCount}}</span></template>
            </el-table-column>
            <el-table-column prop="lossCount" label="盘亏" width="80">
              <template #default="{row}"><span style="color:#F56C6C">{{row.lossCount}}</span></template>
            </el-table-column>
            <el-table-column prop="completionRate" label="准确率" width="100">
              <template #default="{row}">
                <el-progress :percentage="row.completionRate" :color="row.completionRate >= 90 ? '#67C23A' : '#E6A23C'" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card shadow="hover">
          <template #header><span>准确率趋势</span></template>
          <div ref="inventoryChartRef" class="chart-container" style="height:300px"></div>
        </el-card>
      </el-tab-pane>

      <!-- Tab 3: 领用统计 -->
      <el-tab-pane label="领用统计" name="useStats">
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="6">
            <el-card shadow="hover" :body-style="{padding:'20px',textAlign:'center'}">
              <div style="font-size:14px;color:#909399">总领用申请</div>
              <div style="font-size:36px;font-weight:bold;color:#409EFF;margin:8px 0">{{ useStatsData.totalApply }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" :body-style="{padding:'20px',textAlign:'center'}">
              <div style="font-size:14px;color:#909399">平均审批天数</div>
              <div style="font-size:36px;font-weight:bold;color:#67C23A;margin:8px 0">{{ useStatsData.avgApproveDays }}天</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card shadow="hover">
          <template #header><span>月度领用/归还/逾期趋势</span></template>
          <div ref="useStatsChartRef" class="chart-container" style="height:400px"></div>
        </el-card>
      </el-tab-pane>

      <!-- Tab 4: 耗材分析 -->
      <el-tab-pane label="耗材分析" name="consumable">
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{padding:'16px',textAlign:'center'}">
              <div style="font-size:13px;color:#909399">当月消耗量</div>
              <div style="font-size:28px;font-weight:bold;color:#409EFF;margin:4px 0">{{ consumableData.currentMonth?.totalQuantity || 0 }}</div>
              <div style="font-size:12px;color:#909399">金额: ¥{{ consumableData.currentMonth?.totalAmount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{padding:'16px',textAlign:'center'}">
              <div style="font-size:13px;color:#909399">上月消耗量</div>
              <div style="font-size:28px;font-weight:bold;color:#909399;margin:4px 0">{{ consumableData.lastMonth?.totalQuantity || 0 }}</div>
              <div style="font-size:12px;color:#909399">金额: ¥{{ consumableData.lastMonth?.totalAmount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" :body-style="{padding:'16px',textAlign:'center'}">
              <div style="font-size:13px;color:#909399">去年同月消耗量</div>
              <div style="font-size:28px;font-weight:bold;color:#E6A23C;margin:4px 0">{{ consumableData.sameMonthLastYear?.totalQuantity || 0 }}</div>
              <div style="font-size:12px;color:#909399">金额: ¥{{ consumableData.sameMonthLastYear?.totalAmount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header><span>月度消耗对比</span></template>
              <div ref="consumableMonthChartRef" class="chart-container" style="height:350px"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header><span>分类消耗变化</span></template>
              <el-table :data="consumableData.byCategory" stripe size="small" max-height="350">
                <el-table-column prop="category" label="分类" />
                <el-table-column prop="currentQty" label="当月" width="80" />
                <el-table-column prop="lastQty" label="上月" width="80" />
                <el-table-column label="环比" width="100">
                  <template #default="{row}">
                    <span :style="{color: row.growth > 0 ? '#F56C6C' : row.growth < 0 ? '#67C23A' : '#909399'}">
                      {{ row.growth > 0 ? '+' : '' }}{{ row.growth }}%
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- Tab 5: 数据导出 -->
      <el-tab-pane label="数据导出" name="export">
        <el-row :gutter="24">
          <el-col :span="6" v-for="item in exportItems" :key="item.type">
            <el-card shadow="hover" :body-style="{padding:'24px',textAlign:'center'}">
              <el-icon :size="48" :color="item.color"><component :is="item.icon" /></el-icon>
              <div style="margin:12px 0;font-size:16px;font-weight:bold">{{ item.title }}</div>
              <div style="font-size:12px;color:#909399;margin-bottom:16px">{{ item.desc }}</div>
              <el-button type="primary" :icon="Download" @click="doExport(item.type)" :loading="exportLoading === item.type">
                导出 CSV
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Download, Document, Coin, Box, DataLine } from '@element-plus/icons-vue'
import request from '@/utils/request'
import axios from 'axios'

const activeTab = ref('lifecycle')

// ============ Tab 1: 资产生命周期 ============
const lifecycleData = reactive({ byCategory: [], totalAssets: 0 })
const lifecycleChartRef = ref(null)
let lifecycleChart = null

async function loadLifecycle() {
  const r = await request.get('/report/asset-lifecycle')
  if (r.code === 200) {
    lifecycleData.byCategory = r.data.byCategory || []
    lifecycleData.totalAssets = r.data.totalAssets || 0
    await nextTick()
    renderLifecycleChart()
  }
}

function renderLifecycleChart() {
  if (!lifecycleChartRef.value) return
  if (lifecycleChart) lifecycleChart.dispose()
  lifecycleChart = echarts.init(lifecycleChartRef.value)

  const cats = lifecycleData.byCategory.map(c => c.categoryName)
  const inStock = lifecycleData.byCategory.map(c => c.inStock || 0)
  const inUse = lifecycleData.byCategory.map(c => c.inUse || 0)
  const repairing = lifecycleData.byCategory.map(c => c.repairing || 0)
  const scrapped = lifecycleData.byCategory.map(c => c.scrapped || 0)

  lifecycleChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['在库', '使用中', '维修中', '已报废'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: cats, axisLabel: { rotate: 20 } },
    yAxis: { type: 'value' },
    series: [
      { name: '在库', type: 'bar', stack: 'total', data: inStock, itemStyle: { color: '#909399' } },
      { name: '使用中', type: 'bar', stack: 'total', data: inUse, itemStyle: { color: '#67C23A' } },
      { name: '维修中', type: 'bar', stack: 'total', data: repairing, itemStyle: { color: '#E6A23C' } },
      { name: '已报废', type: 'bar', stack: 'total', data: scrapped, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

// ============ Tab 2: 盘点汇总 ============
const inventoryData = reactive({ recentInventories: [], overallAccuracy: 0 })
const inventoryChartRef = ref(null)
let inventoryChart = null

async function loadInventory() {
  const r = await request.get('/report/inventory-summary')
  if (r.code === 200) {
    inventoryData.recentInventories = r.data.recentInventories || []
    inventoryData.overallAccuracy = r.data.overallAccuracy || 0
    await nextTick()
    renderInventoryChart()
  }
}

function renderInventoryChart() {
  if (!inventoryChartRef.value || !inventoryData.recentInventories.length) return
  if (inventoryChart) inventoryChart.dispose()
  inventoryChart = echarts.init(inventoryChartRef.value)

  const reversed = [...inventoryData.recentInventories].reverse()
  const dates = reversed.map(d => d.date?.substring(0, 10) || '')
  const rates = reversed.map(d => d.completionRate || 0)

  inventoryChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 20 } },
    yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      name: '准确率', type: 'line', data: rates, smooth: true,
      itemStyle: { color: '#67C23A' }, areaStyle: { color: 'rgba(103,194,58,0.1)' },
      markLine: { data: [{ type: 'average', name: '平均值' }] }
    }]
  })
}

// ============ Tab 3: 领用统计 ============
const useStatsData = reactive({ monthlyStats: [], totalApply: 0, avgApproveDays: 0 })
const useStatsChartRef = ref(null)
let useStatsChart = null

async function loadUseStats() {
  const r = await request.get('/report/use-statistics')
  if (r.code === 200) {
    useStatsData.monthlyStats = r.data.monthlyStats || []
    useStatsData.totalApply = r.data.totalApply || 0
    useStatsData.avgApproveDays = r.data.avgApproveDays || 0
    await nextTick()
    renderUseStatsChart()
  }
}

function renderUseStatsChart() {
  if (!useStatsChartRef.value) return
  if (useStatsChart) useStatsChart.dispose()
  useStatsChart = echarts.init(useStatsChartRef.value)

  const months = useStatsData.monthlyStats.map(s => s.month)
  const apply = useStatsData.monthlyStats.map(s => s.applyCount || 0)
  const approve = useStatsData.monthlyStats.map(s => s.approveCount || 0)
  const returns = useStatsData.monthlyStats.map(s => s.returnCount || 0)
  const overdue = useStatsData.monthlyStats.map(s => s.overdueCount || 0)

  useStatsChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['申请数', '审批通过', '归还数', '逾期数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months, axisLabel: { rotate: 20 } },
    yAxis: { type: 'value' },
    series: [
      { name: '申请数', type: 'line', data: apply, smooth: true, itemStyle: { color: '#409EFF' } },
      { name: '审批通过', type: 'line', data: approve, smooth: true, itemStyle: { color: '#67C23A' } },
      { name: '归还数', type: 'line', data: returns, smooth: true, itemStyle: { color: '#E6A23C' } },
      { name: '逾期数', type: 'line', data: overdue, smooth: true, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

// ============ Tab 4: 耗材分析 ============
const consumableData = reactive({
  currentMonth: null, lastMonth: null, sameMonthLastYear: null, byCategory: []
})
const consumableMonthChartRef = ref(null)
let consumableMonthChart = null

async function loadConsumable() {
  const r = await request.get('/report/consumable-comparison')
  if (r.code === 200) {
    Object.assign(consumableData, r.data)
    await nextTick()
    renderConsumableChart()
  }
}

function renderConsumableChart() {
  if (!consumableMonthChartRef.value) return
  if (consumableMonthChart) consumableMonthChart.dispose()
  consumableMonthChart = echarts.init(consumableMonthChartRef.value)

  const curQty = consumableData.currentMonth?.totalQuantity || 0
  const lastQty = consumableData.lastMonth?.totalQuantity || 0
  const yoyQty = consumableData.sameMonthLastYear?.totalQuantity || 0

  consumableMonthChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['消耗量', '金额(元)'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['去年同月', '上月', '当月'] },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '金额(元)' }
    ],
    series: [
      {
        name: '消耗量', type: 'bar', data: [yoyQty, lastQty, curQty],
        itemStyle: { color: '#409EFF' }, barWidth: '40%'
      },
      {
        name: '金额(元)', type: 'bar', yAxisIndex: 1,
        data: [
          consumableData.sameMonthLastYear?.totalAmount || 0,
          consumableData.lastMonth?.totalAmount || 0,
          consumableData.currentMonth?.totalAmount || 0
        ],
        itemStyle: { color: '#E6A23C' }, barWidth: '40%'
      }
    ]
  })
}

// ============ Tab 5: 数据导出 ============
const exportLoading = ref(null)
const exportItems = [
  { type: 'assets', title: '资产数据', desc: '导出当前站点所有资产明细', icon: Document, color: '#409EFF' },
  { type: 'depreciation', title: '折旧报表', desc: '导出资产折旧明细数据', icon: Coin, color: '#67C23A' },
  { type: 'inventory', title: '盘点数据', desc: '导出最近一次盘点明细', icon: DataLine, color: '#E6A23C' },
  { type: 'consumable', title: '耗材数据', desc: '导出耗材出库记录', icon: Box, color: '#909399' }
]

async function doExport(type) {
  exportLoading.value = type
  try {
    const response = await axios.get('/asset/report/export', {
      params: { type, format: 'csv', site: localStorage.getItem('site') },
      responseType: 'blob',
      headers: { token: localStorage.getItem('token') }
    })
    const blob = new Blob([response.data], { type: 'text/csv;charset=UTF-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const names = { assets: '资产数据', depreciation: '折旧报表', inventory: '盘点数据', consumable: '耗材数据' }
    a.download = names[type] + '_' + new Date().toISOString().slice(0, 10) + '.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('导出失败：' + (e.response?.status ? 'HTTP ' + e.response.status : e.message || '网络错误'))
  } finally {
    exportLoading.value = null
  }
}

// ============ Tab 切换加载 ============
const loadedTabs = reactive({})
function onTabClick(tab) {
  const name = tab.paneName
  if (loadedTabs[name]) return
  loadedTabs[name] = true
  switch (name) {
    case 'lifecycle': loadLifecycle(); break
    case 'inventory': loadInventory(); break
    case 'useStats': loadUseStats(); break
    case 'consumable': loadConsumable(); break
  }
}

// ============ 监听站点切换（通过localStorage） ============
watch(() => localStorage.getItem('site'), () => {
  // 重置已加载标记，重新加载数据
  Object.keys(loadedTabs).forEach(k => delete loadedTabs[k])
  const name = activeTab.value
  loadedTabs[name] = true
  switch (name) {
    case 'lifecycle': loadLifecycle(); break
    case 'inventory': loadInventory(); break
    case 'useStats': loadUseStats(); break
    case 'consumable': loadConsumable(); break
  }
})

onMounted(() => {
  // 默认加载第一个Tab
  loadedTabs.lifecycle = true
  loadLifecycle()

  // 窗口大小变化时重绘图表
  window.addEventListener('resize', () => {
    lifecycleChart?.resize()
    inventoryChart?.resize()
    useStatsChart?.resize()
    consumableMonthChart?.resize()
  })
})
</script>

<style scoped>
.reports {
  min-height: 500px;
}

:deep(.el-tabs__content) {
  padding: 16px;
}

.chart-container {
  padding: 20px;
  min-height: 300px;
}
</style>
