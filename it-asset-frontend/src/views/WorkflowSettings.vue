<template>
  <div class="workflow-settings">
    <div class="page-header">
      <h2>审批流设置</h2>
      <p style="color:#909399;font-size:13px">配置各模块审批级数、审批人及触发条件</p>
    </div>

    <div v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="12" v-for="item in tableData" :key="item.id" style="margin-bottom:16px">
          <el-card shadow="hover">
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span style="font-weight:bold;font-size:15px">{{ item.moduleName }}</span>
                <el-switch
                  v-model="item.requireApproval"
                  :active-value="1" :inactive-value="0"
                  active-text="审批" inactive-text="免审"
                  @change="save(item)"
                />
              </div>
            </template>

            <template v-if="item.requireApproval === 1">
              <!-- 审批级数 -->
              <div style="margin-bottom:12px">
                <span style="font-size:13px;color:#606266;margin-right:8px">审批级数:</span>
                <el-radio-group v-model="item.approvalLevels" size="small" @change="save(item)">
                  <el-radio-button :value="1">一级</el-radio-button>
                  <el-radio-button :value="2">二级</el-radio-button>
                  <el-radio-button :value="3">三级</el-radio-button>
                </el-radio-group>
              </div>

              <!-- 审批人配置 -->
              <div style="margin-bottom:8px">
                <div v-for="lv in item.approvalLevels" :key="lv" style="margin:6px 0;display:flex;align-items:center;gap:8px">
                  <el-tag size="small" type="primary">L{{ lv }}</el-tag>
                  <el-select
                    v-model="item['level' + lv + 'Approver']"
                    placeholder="选择审批人"
                    size="small"
                    style="width:160px"
                    filterable
                    allow-create
                    @change="save(item)"
                  >
                    <el-option label="管理员" value="管理员" />
                    <el-option label="部门主管" value="部门主管" />
                    <el-option label="IT经理" value="IT经理" />
                    <el-option label="财务主管" value="财务主管" />
                    <el-option label="资产管理员" value="资产管理员" />
                  </el-select>
                  <span v-if="lv === 1" style="font-size:12px;color:#909399">提交后</span>
                  <span v-else style="font-size:12px;color:#909399">← L{{ lv-1 }}通过后</span>
                  <el-icon v-if="lv < item.approvalLevels" style="color:#67C23A"><ArrowDown /></el-icon>
                </div>
              </div>

              <!-- 自动审批 -->
              <div style="margin-top:8px;display:flex;align-items:center;gap:8px">
                <el-switch
                  v-model="item.autoApprove"
                  :active-value="1" :inactive-value="0"
                  size="small"
                  @change="save(item)"
                />
                <span style="font-size:12px;color:#909399">符合条件的自动通过</span>
              </div>
              <div v-if="item.autoApprove === 1" style="margin-top:6px;display:flex;gap:8px;align-items:center">
                <el-select v-model="item.conditionField" size="small" style="width:120px" @change="save(item)">
                  <el-option label="采购金额" value="purchasePrice" />
                  <el-option label="资产类别" value="categoryId" />
                </el-select>
                <el-input v-model="item.conditionValue" size="small" placeholder="阈值" style="width:100px" @blur="save(item)" />
                <span style="font-size:12px;color:#909399">以下自动通过</span>
              </div>
            </template>
            <div v-else style="color:#67C23A;font-size:13px">无需审批，提交后直接生效</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await request.get('/workflow-config/list')
    if (res.code === 200) tableData.value = res.data || []
  } catch (e) { ElMessage.error('加载失败') }
  loading.value = false
}

const save = async (row) => {
  try {
    const res = await request.post('/workflow-config/update', row)
    if (res.code === 200) ElMessage.success(`${row.moduleName} 已保存`)
    else ElMessage.error(res.msg)
  } catch (e) { ElMessage.error('保存失败') }
}

onMounted(loadConfig)
</script>

<style scoped>
.workflow-settings { padding: 16px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 4px 0; font-size: 18px; }
</style>
