<template>
  <div class="login-page">
    <!-- 装饰层：极淡几何纹理（纯 CSS，aria-hidden） -->
    <div class="brand-decor" aria-hidden="true">
      <span class="decor-rings"></span>
      <span class="decor-wave"></span>
    </div>

    <!-- ============ 左栏：品牌区 ============ -->
    <section class="brand-panel">
      <div class="brand-content">
        <!-- NAI logo：白底图，装进白色圆角 chip，避免深色背景上的白方块 -->
        <div class="brand-logo-chip">
          <img src="/nai-logo.png" alt="NAI - Reliable Connectivity Solutions" class="brand-logo" />
        </div>

        <h1 class="brand-title">IT 固定资产管理系统</h1>
        <p class="brand-subtitle">IT Fixed Asset Management System</p>

        <!-- 蓝色短横线 -->
        <span class="brand-accent-line"></span>

        <p class="brand-slogan">专业 · 高效 · 安全 · 智能</p>
        <p class="brand-desc">助力企业实现 IT 资产全生命周期管理</p>

        <!-- 底部 4 个能力卡片 -->
        <div class="capability-row">
          <div class="cap-card">
            <span class="cap-icon"><el-icon><Box /></el-icon></span>
            <span class="cap-title">资产全生命周期</span>
            <span class="cap-sub">采购·使用·维护·报废</span>
          </div>
          <div class="cap-card">
            <span class="cap-icon"><el-icon><PieChart /></el-icon></span>
            <span class="cap-title">数据可视化</span>
            <span class="cap-sub">多维报表·洞察分析</span>
          </div>
          <div class="cap-card">
            <span class="cap-icon"><el-icon><Lock /></el-icon></span>
            <span class="cap-title">安全合规</span>
            <span class="cap-sub">权限管控·数据安全</span>
          </div>
          <div class="cap-card">
            <span class="cap-icon"><el-icon><Cloudy /></el-icon></span>
            <span class="cap-title">高效协同</span>
            <span class="cap-sub">流程驱动·提升效率</span>
          </div>
        </div>
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

        <div class="form-card">
          <!-- 标题区：图标块 + 两行文字 -->
          <div class="form-head">
            <span class="head-icon"><el-icon><UserFilled /></el-icon></span>
            <div class="head-text">
              <h2 class="form-title">欢迎登录</h2>
              <p class="form-subtitle">请输入您的账号信息以继续</p>
            </div>
          </div>

          <div class="form-divider"></div>

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

          <!-- 提示区：铃铛图标块 + 两行文字 -->
          <div class="form-tips">
            <span class="tips-icon"><el-icon><Bell /></el-icon></span>
            <div class="tips-text">
              <p class="tips-line">忘记密码请向管理员申请重置，</p>
              <p class="tips-line">请妥善保管账号信息</p>
            </div>
          </div>
        </div>

        <div class="form-copyright">© {{ currentYear }} NAI · IT Asset Management</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Box, PieChart, Cloudy, UserFilled, Bell } from '@element-plus/icons-vue'
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
   1. 页面容器：左右分栏（左 57% 品牌区 / 右 43% 表单区）
   ============================================================ */
.login-page {
  position: relative;
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #051A39;
}

/* ============================================================
   2. 左栏：品牌区
   ============================================================ */
.brand-panel {
  position: relative;
  z-index: 2;
  flex: 0 0 57%;
  display: flex;
  align-items: center;
  padding: 0 6% 0 7%;
  overflow: hidden;
  background: linear-gradient(180deg, #051A39 0%, #041B3B 45%, #021535 100%);
}

.brand-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 640px;
  margin-top: -2vh;
}

/* ---- NAI logo：白底 chip（放大版）---- */
.brand-logo-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 22px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
}

.brand-logo {
  display: block;
  height: 62px;
  width: auto;
}

/* ---- 品牌文案 ---- */
.brand-title {
  margin: 34px 0 0;
  font-size: 44px;
  font-weight: 800;
  letter-spacing: 2px;
  color: #ffffff;
  line-height: 1.2;
}

.brand-subtitle {
  margin: 14px 0 0;
  font-size: 14px;
  letter-spacing: 0.2em;
  color: #8FA0C0;
  text-transform: uppercase;
}

/* 蓝色短横线 */
.brand-accent-line {
  display: block;
  width: 48px;
  height: 3px;
  margin: 26px 0 0;
  border-radius: 2px;
  background: #277BF7;
}

.brand-slogan {
  margin: 22px 0 0;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #ffffff;
}

.brand-desc {
  margin: 12px 0 0;
  font-size: 15px;
  color: #8FA0C0;
}

/* ---- 4 个能力卡片 ---- */
.capability-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-top: 54px;
}

.cap-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  flex: 1 1 0;
  min-width: 0;
}

.cap-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #277BF7, #4FC3F7);
  color: #ffffff;
  font-size: 22px;
}

.cap-title {
  margin-top: 14px;
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.3;
}

.cap-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #8FA0C0;
  line-height: 1.4;
}

/* ============================================================
   3. 背景装饰（极淡，不喧宾夺主）
   ============================================================ */
.brand-decor {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

/* 中部偏右的大半径同心弧线 */
.decor-rings {
  position: absolute;
  top: -10%;
  left: 34%;
  width: 1000px;
  height: 1000px;
  border-radius: 50%;
  background: repeating-radial-gradient(
    circle at center,
    transparent 0 62px,
    rgba(255, 255, 255, 0.06) 62px 63px
  );
  -webkit-mask-image: radial-gradient(circle at center, #000 26%, transparent 74%);
  mask-image: radial-gradient(circle at center, #000 26%, transparent 74%);
}

/* 底部波浪点阵纹理 */
.decor-wave {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 42%;
  background-image: radial-gradient(rgba(255, 255, 255, 0.10) 1.2px, transparent 1.3px);
  background-size: 16px 16px;
  -webkit-mask-image: linear-gradient(180deg, transparent 0%, #000 60%, #000 100%);
  mask-image: linear-gradient(180deg, transparent 0%, #000 60%, #000 100%);
}

/* ============================================================
   4. 右栏：表单区
   ============================================================ */
.form-panel {
  position: relative;
  z-index: 2;
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(180deg, #0D2446 0%, #0C2140 60%, #0B1B3D 100%);
}

.form-inner {
  width: 86%;
  max-width: 460px;
}

/* ---- 窄屏品牌头（默认隐藏）---- */
.form-brand-mini {
  display: none;
  margin-bottom: 28px;
  text-align: center;
}

.brand-logo-chip--mini {
  padding: 12px 18px;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
}

.brand-logo-chip--mini .brand-logo {
  height: 44px;
}

.form-brand-mini-title {
  margin: 16px 0 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #ffffff;
}

/* ---- 表单卡片 ---- */
.form-card {
  padding: 32px;
  border-radius: 16px;
  background: #243755;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.28);
}

/* 标题区 */
.form-head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.head-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #277BF7, #4FC3F7);
  color: #ffffff;
  font-size: 22px;
}

.head-text {
  min-width: 0;
}

.form-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  line-height: 1.2;
}

.form-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: #8FA0C0;
}

.form-divider {
  height: 1px;
  margin: 22px 0 26px;
  background: rgba(255, 255, 255, 0.08);
}

/* ---- 表单主体 ---- */
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  height: 45px;
  border-radius: 8px;
  background-color: #263A55;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.10) inset;
  transition: box-shadow 0.2s, background-color 0.2s;
  padding: 1px 12px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.22) inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: #263A55;
  box-shadow: 0 0 0 1px #277BF7 inset;
}

.login-form :deep(.el-input__inner) {
  height: 45px;
  color: #ffffff;
  caret-color: #ffffff;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #8FA0C0;
}

/* 前缀图标 / 清除与密码可见性图标 */
.login-form :deep(.el-input__prefix),
.login-form :deep(.el-input__suffix),
.login-form :deep(.el-input__suffix .el-icon) {
  color: #8FA0C0;
}

/* 浏览器自动填充时仍保持深色底浅色字 */
.login-form :deep(.el-input__inner:-webkit-autofill),
.login-form :deep(.el-input__inner:-webkit-autofill:hover),
.login-form :deep(.el-input__inner:-webkit-autofill:focus) {
  -webkit-text-fill-color: #ffffff;
  -webkit-box-shadow: 0 0 0 1000px #263A55 inset;
}

.login-btn-group {
  margin-top: 28px;
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  background: #277BF7;
}

.login-btn:hover,
.login-btn:focus {
  background: #3d8bf8;
}

/* ---- 提示区 ---- */
.form-tips {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #1A2C50;
}

.tips-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: #8FA0C0;
  font-size: 16px;
}

.tips-text {
  min-width: 0;
}

.tips-line {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: #8FA0C0;
}

/* ---- 版权 ---- */
.form-copyright {
  margin-top: 22px;
  text-align: center;
  font-size: 12px;
  color: #6B7A99;
  letter-spacing: 0.5px;
}

/* ============================================================
   5. 响应式：<=900px 改为单栏
   ============================================================ */
@media (max-width: 900px) {
  .login-page {
    flex-direction: column;
    overflow-y: auto;
    overflow-x: hidden;
    height: auto;
    min-height: 100vh;
  }

  /* 隐藏左侧大品牌区 */
  .brand-panel {
    display: none;
  }

  .form-panel {
    flex: 1 1 auto;
    min-height: 100vh;
    padding: 40px 24px;
  }

  .form-inner {
    width: min(420px, calc(100% - 48px));
    max-width: none;
  }

  /* 窄屏顶部显示小 logo + 系统名 */
  .form-brand-mini {
    display: block;
  }

  /* 单栏下隐藏绝对定位的整页装饰，避免错位 */
  .brand-decor {
    display: none;
  }
}
</style>
