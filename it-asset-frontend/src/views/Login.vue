<template>
  <div class="login-page">
    <!-- ============ 左栏：品牌区 ============ -->
    <section class="brand-panel">
      <!-- 装饰层：低透明度几何元素（纯 CSS，aria-hidden） -->
      <div class="brand-decor" aria-hidden="true">
        <span class="decor-ring decor-ring-1"></span>
        <span class="decor-ring decor-ring-2"></span>
        <span class="decor-ring decor-ring-3"></span>
        <span class="decor-grid"></span>
        <span class="decor-beam"></span>
      </div>

      <div class="brand-content">
        <!-- NAI logo：白底图，放白色圆角 chip 内，避免深色背景上的白方块 -->
        <div class="brand-logo-chip">
          <img src="/nai-logo.png" alt="NAI - Reliable Connectivity Solutions" class="brand-logo" />
        </div>

        <h1 class="brand-title">IT 固定资产管理系统</h1>
        <p class="brand-subtitle">IT Fixed Asset Management System</p>

        <div class="brand-divider"></div>

        <ul class="brand-desc">
          <li>资产全生命周期管理</li>
          <li>苏州 · Penang 双站点协同</li>
          <li>覆盖入库、领用、维修、报废等全流程</li>
        </ul>
      </div>
    </section>

    <!-- ============ 右栏：表单区 ============ -->
    <section class="form-panel">
      <div class="form-inner">
        <!-- 窄屏品牌头（仅在 <=900px 显示） -->
        <div class="form-brand-mini">
          <div class="brand-logo-chip brand-logo-chip--mini">
            <img src="/nai-logo.png" alt="NAI" class="brand-logo" />
          </div>
          <h1 class="form-brand-mini-title">IT 固定资产管理系统</h1>
        </div>

        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号信息以继续</p>

        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="login-btn-group">
            <el-button
              type="primary"
              @click="handleLogin"
              class="login-btn"
              :loading="loading"
              size="large"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 轻量提示区（替代原先的大块 el-alert） -->
        <div class="form-tips">
          <p class="form-tips-main">首次登录请联系管理员获取账号</p>
          <p class="form-tips-sub">忘记密码请向管理员申请重置 · 请妥善保管账号信息</p>
        </div>

        <div class="form-copyright">© {{ currentYear }} NAI · IT Asset Management</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 版权年份（纯展示）
const currentYear = new Date().getFullYear()

const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 先进行表单验证
    await loginFormRef.value.validate()

    loading.value = true

    // 调用登录接口
    const res = await request.post('/login', loginForm)

    // 保存 token 和用户信息
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('realName', res.data.realName || res.data.username)
    localStorage.setItem('userId', res.data.userId)
    localStorage.setItem('role', res.data.role)
    if (res.data.site) localStorage.setItem('site', res.data.site)

    ElMessage.success('登录成功！')

    // 默认密码警告
    if (res.data.isDefaultPassword) {
      setTimeout(() => {
        ElMessageBox.confirm('您的密码仍为系统默认密码，存在安全风险。\n\n建议立即修改密码，是否前往修改？', '安全提醒', {
          confirmButtonText: '去修改', cancelButtonText: '暂不修改', type: 'warning'
        }).then(() => {
          router.push('/change-password')
        }).catch(() => {
          router.push('/home')
        })
      }, 800)
    } else {
      setTimeout(() => router.push('/home'), 500)
    }

  } catch (error) {
    // 表单验证失败时，Element Plus 会自动显示错误信息，不需要额外弹窗
    // 网络请求失败时，request.js 拦截器已经处理了错误弹窗
    // 仅在确认不是表单验证错误时显示额外消息
    if (error.response || (error.message && error.message !== 'cancel')) {
      // request.js 拦截器已经弹窗过，这里不重复弹窗
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ============================================================
   1. 页面容器：左右分栏，全屏 100vh
   ============================================================ */
.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

/* ============================================================
   2. 左栏：品牌区（深藏青，与系统侧栏同色系）
   ============================================================ */
.brand-panel {
  position: relative;
  flex: 0 0 55%;
  display: flex;
  align-items: center;
  padding: 0 8%;
  overflow: hidden;
  background: linear-gradient(160deg, #1f2d3d 0%, #304156 55%, #2b3a4b 100%);
}

.brand-content {
  position: relative;
  z-index: 2;
  max-width: 560px;
  margin-top: -6vh; /* 视觉垂直居中偏上 */
}

/* ---- NAI logo：白底 chip ---- */
.brand-logo-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 18px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
}

.brand-logo {
  display: block;
  height: 40px;
  width: auto;
}

/* ---- 品牌文案 ---- */
.brand-title {
  margin: 30px 0 0;
  font-size: 38px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #ffffff;
  line-height: 1.25;
}

.brand-subtitle {
  margin: 12px 0 0;
  font-size: 15px;
  letter-spacing: 1.5px;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
}

.brand-divider {
  width: 64px;
  height: 3px;
  margin: 26px 0;
  border-radius: 2px;
  background: #409EFF;
}

.brand-desc {
  list-style: none;
  margin: 0;
  padding: 0;
}

.brand-desc li {
  position: relative;
  padding-left: 18px;
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
}

.brand-desc li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.85);
}

/* ---- 装饰层（低透明度几何元素）---- */
.brand-decor {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

/* 同心圆环（右下角） */
.decor-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid #ffffff;
}

.decor-ring-1 {
  width: 560px;
  height: 560px;
  right: -190px;
  bottom: -220px;
  opacity: 0.05;
}

.decor-ring-2 {
  width: 400px;
  height: 400px;
  right: -110px;
  bottom: -140px;
  opacity: 0.07;
}

.decor-ring-3 {
  width: 250px;
  height: 250px;
  right: -35px;
  bottom: -55px;
  opacity: 0.09;
}

/* 细网格线 */
.decor-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(#ffffff 1px, transparent 1px),
    linear-gradient(90deg, #ffffff 1px, transparent 1px);
  background-size: 52px 52px;
  opacity: 0.04;
  -webkit-mask-image: linear-gradient(115deg, transparent 45%, #000 100%);
  mask-image: linear-gradient(115deg, transparent 45%, #000 100%);
}

/* 斜向光带 */
.decor-beam {
  position: absolute;
  top: -25%;
  right: 14%;
  width: 160px;
  height: 150%;
  transform: rotate(18deg);
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 1) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  opacity: 0.05;
  filter: blur(3px);
}

/* ============================================================
   3. 右栏：表单区（白底居中）
   ============================================================ */
.form-panel {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 24px;
}

.form-inner {
  width: 400px;
  max-width: calc(100% - 48px);
}

/* ---- 窄屏品牌头（默认隐藏）---- */
.form-brand-mini {
  display: none;
  margin-bottom: 32px;
}

.brand-logo-chip--mini {
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

.brand-logo-chip--mini .brand-logo {
  height: 28px;
}

.form-brand-mini-title {
  margin: 16px 0 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #303133;
}

/* ---- 表单标题 ---- */
.form-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.form-subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #909399;
}

/* ---- 表单主体 ---- */
.login-form {
  margin-top: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.login-form :deep(.el-input__inner) {
  height: 44px;
}

.login-btn-group {
  margin-top: 30px;
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  border-radius: 8px;
}

/* ---- 轻量提示区 ---- */
.form-tips {
  margin-top: 24px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid #ebeef5;
}

.form-tips p {
  margin: 0;
  line-height: 1.6;
}

.form-tips-main {
  font-size: 13px;
  color: #606266;
}

.form-tips-sub {
  margin-top: 4px !important;
  font-size: 12px;
  color: #a0a4ab;
}

/* ---- 版权 ---- */
.form-copyright {
  margin-top: 40px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  letter-spacing: 0.5px;
}

/* ============================================================
   4. 响应式：<=900px 改为单栏
   ============================================================ */
@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
    overflow-y: auto;
  }

  /* 隐藏左侧大品牌区 */
  .brand-panel {
    display: none;
  }

  .form-panel {
    flex: 1 1 auto;
    min-height: 100vh;
    padding: 40px 24px;
    background: #ffffff;
  }

  /* 窄屏顶部显示小 logo + 标题 */
  .form-brand-mini {
    display: block;
  }

  .form-title {
    font-size: 22px;
  }
}
</style>
