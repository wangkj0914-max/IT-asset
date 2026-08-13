<template>
  <div class="mobile-home">
    <div class="home-header">
      <h1>IT资产管理系统</h1>
      <p>{{ currentSite }} 站点</p>
      <p class="user-info">{{ realName }} · {{ isAdmin ? '管理员' : '普通用户' }}</p>
    </div>
    
    <div class="menu-grid">
      <div class="menu-card scan" @click="$router.push('/mobile-scan')">
        <div class="menu-icon">📷</div>
        <div class="menu-title">扫码盘点</div>
        <div class="menu-desc">扫描资产二维码，快速盘点</div>
      </div>
      <div class="menu-card query" @click="$router.push('/mobile-query')">
        <div class="menu-icon">🔍</div>
        <div class="menu-title">资产查询</div>
        <div class="menu-desc">按编号或名称搜索资产信息</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const currentSite = localStorage.getItem('site') || ''
const realName = localStorage.getItem('realName') || localStorage.getItem('username') || ''
const role = parseInt(localStorage.getItem('role') || '1')
const isAdmin = computed(() => role === 2)
</script>

<style scoped>
.mobile-home {
  max-width: 500px;
  margin: 0 auto;
  padding: 24px 16px;
  min-height: 100vh;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 60%, #028090 100%);
}
.home-header {
  text-align: center;
  padding: 32px 0 24px;
}
.home-header h1 {
  font-size: 22px;
  color: white;
  margin: 0 0 8px;
}
.home-header p {
  color: #CBD5E1;
  margin: 4px 0;
  font-size: 14px;
}
.user-info {
  color: #02C39A !important;
  font-size: 13px !important;
}
.menu-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 0;
}
.menu-card {
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 16px;
  padding: 28px 24px;
  cursor: pointer;
  transition: transform 0.15s, background 0.15s;
}
.menu-card:active {
  transform: scale(0.98);
  background: rgba(255,255,255,0.14);
}
.menu-icon { font-size: 36px; margin-bottom: 12px; }
.menu-title { font-size: 20px; font-weight: bold; color: white; margin-bottom: 6px; }
.menu-desc { font-size: 13px; color: #94A3B8; }
</style>
