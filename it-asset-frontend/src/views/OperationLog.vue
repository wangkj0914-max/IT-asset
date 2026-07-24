<template>
  <div class="log-container">
    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="模块">
          <el-select v-model="searchModule" placeholder="全部" clearable @change="loadData">
            <el-option label="资产管理" value="资产管理" />
            <el-option label="资产维修" value="资产维修" />
            <el-option label="资产报废" value="资产报废" />
            <el-option label="资产盘点" value="资产盘点" />
            <el-option label="资产领用" value="资产领用" />
            <el-option label="资产归还" value="资产归还" />
            <el-option label="资产调拨" value="资产调拨" />
            <el-option label="资产入库" value="资产入库" />
            <el-option label="资产分类" value="资产分类" />
            <el-option label="部门管理" value="部门管理" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="系统管理" value="系统管理" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchUser" placeholder="用户名" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" clearable @change="loadData">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading" size="small">
        <el-table-column prop="logId" label="ID" width="70" />
        <el-table-column prop="userName" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operation" label="操作" width="80">
          <template #default="{ row }">
            <el-tag :type="opColor(row.operation)" size="small">{{ row.operation }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="requestParams" label="请求参数" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.requestParams || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.errorMsg || '-' }}</template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="costTime" label="耗时" width="70">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无操作记录" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top:16px;justify-content:flex-end"
        @size-change="loadData" @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false), tableData = ref([]), pageNum = ref(1), pageSize = ref(20), total = ref(0)
const searchModule = ref(null), searchUser = ref(''), searchStatus = ref(null)

const opColor = (o) => ({ ADD: 'success', UPDATE: 'primary', DELETE: 'danger', APPROVE: 'warning', COMPLETE: 'success', RESET: 'info' }[o] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/log/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, module: searchModule.value, userName: searchUser.value, status: searchStatus.value }
    })
    if (res.code === 200) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch { ElMessage.error('加载失败') } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.log-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { min-height: 400px; }
</style>
