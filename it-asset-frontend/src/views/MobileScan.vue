<template>
  <div class="scan-container">
    <div class="scan-header">
      <span>扫码盘点</span>
      <el-tag v-if="currentSite" type="info" size="small" style="margin-left:12px">{{ currentSite }}</el-tag>
    </div>

    <div v-if="!scanResult" style="text-align:center">
      <div id="qr-reader" style="width:100%;max-width:400px;margin:0 auto"></div>
      <div v-if="!cameraReady && !cameraError" class="scan-hint">
        <el-button type="primary" size="large" @click="startScan" :loading="scanning" style="width:80%;margin:16px 0">
          <el-icon><Camera /></el-icon> 启动摄像头
        </el-button>
        <p class="sub-hint">对准二维码，自动识别盘点</p>
        <p class="sub-hint" style="color:#DC2626" v-if="isHttp">如无法启动，请用 HTTPS: https://{{ host }}:8082/mobile-scan</p>
      </div>
      <div v-if="scanning" class="scan-status">
        <el-icon class="is-loading"><Loading /></el-icon> 摄像头已启动，对准二维码即可自动识别
      </div>
      <div v-if="cameraError" class="scan-hint" style="color:#DC2626">
        <p>⚠️ 摄像头不可用（需 HTTPS）</p>
        <p style="font-size:13px">请访问: <b>https://{{ host }}:8082/mobile-scan</b></p>
        <el-button size="small" @click="startScan" style="margin-top:8px">重试</el-button>
      </div>
      <el-divider>或手动输入</el-divider>
      <el-input v-model="manualCode" placeholder="输入资产编号" clearable size="large" @keyup.enter="lookupByInput" style="max-width:350px">
        <template #append><el-button @click="lookupByInput" :loading="inputLoading">查询</el-button></template>
      </el-input>
    </div>

    <div v-if="scanResult" class="scan-result">
      <el-alert :title="scanResult.assetName" :type="resultType" show-icon :closable="false">
        <template #default>
          <p>资产编号：{{ scanResult.assetCode }}</p>
          <p>品牌型号：{{ scanResult.brand }} {{ scanResult.model }}</p>
          <p>存放位置：{{ scanResult.location || '-' }}</p>
          <p>当前使用人：{{ scanResult.currentUser || '在库' }}</p>
        </template>
      </el-alert>
      <div style="margin-top:16px;display:flex;gap:12px;justify-content:center">
        <el-button type="success" @click="markChecked" :loading="checking">确认盘点</el-button>
        <el-button @click="resetScanResult">继续</el-button>
      </div>
    </div>

    <div v-if="checkedList.length > 0" class="checked-list">
      <el-divider />
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>已盘点：{{ checkedList.length }} 件</span>
        <el-button size="small" type="danger" @click="clearAll">清空记录</el-button>
      </div>
      <el-table :data="checkedList" size="small" max-height="300" style="margin-top:8px">
        <el-table-column prop="assetCode" label="编号" width="120" />
        <el-table-column prop="assetName" label="名称" />
        <el-table-column prop="location" label="位置" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'primary'" size="small">{{ row.status === 0 ? '在库' : '已领用' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Loading } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'
import request from '@/utils/request'

const scanning = ref(false), cameraReady = ref(false), cameraError = ref(false)
const scanResult = ref(null), checking = ref(false), checkedList = ref([])
const currentSite = ref(localStorage.getItem('site') || '')
const manualCode = ref(''), inputLoading = ref(false)
const resultType = ref('success')
const isHttp = computed(() => location.protocol === 'http:')
const host = computed(() => location.hostname)

let scanner = null

const startScan = async () => {
  cameraError.value = false
  try {
    scanning.value = true
    scanner = new Html5Qrcode('qr-reader', { verbose: false })
    await scanner.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: 280 },
      async (decodedText) => {
        await scanner.stop()
        scanning.value = false
        cameraReady.value = false
        lookupAsset(decodedText)
      },
      () => {}
    )
    cameraReady.value = true
  } catch {
    scanning.value = false
    cameraError.value = true
  }
}

const lookupAsset = async (code) => {
  try {
    const res = await request.get('/inventory/scan', { params: { code } })
    if (res.code === 200) {
      scanResult.value = res.data
      resultType.value = 'success'
      ElMessage.success('识别成功')
    } else {
      ElMessage.error(res.msg || '未找到')
    }
  } catch { ElMessage.error('查找失败') }
}

const lookupByInput = () => {
  if (!manualCode.value.trim()) { ElMessage.warning('请输入资产编号'); return }
  inputLoading.value = true
  lookupAsset(manualCode.value.trim()).finally(() => { inputLoading.value = false })
}

const markChecked = () => {
  checking.value = true
  if (!checkedList.value.find(c => c.assetCode === scanResult.value.assetCode)) {
    checkedList.value.unshift({ ...scanResult.value })
  }
  checking.value = false
  scanResult.value = null
  manualCode.value = ''
  ElMessage.success('已记录')
}

const resetScanResult = () => { scanResult.value = null; manualCode.value = '' }
const clearAll = () => { checkedList.value = []; ElMessage.success('已清空') }
</script>

<style scoped>
.scan-container { max-width:500px; margin:0 auto; padding:16px; min-height:100vh; background:#F8FAFC; }
.scan-header { font-size:20px; font-weight:bold; color:#1A1A2E; margin-bottom:16px; display:flex; align-items:center; }
.scan-hint { text-align:center; padding:16px 0; color:#64748B; }
.sub-hint { font-size:12px; color:#94A3B8; margin-top:4px; }
.scan-status { text-align:center; color:#028090; padding:16px 0; }
.scan-result { margin-top:16px; }
.scan-result p { margin:4px 0; font-size:13px; color:#475569; }
.checked-list { margin-top:20px; }
</style>
