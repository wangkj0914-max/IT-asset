<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-title">IT 固定资产管理系统</div>
      <div class="login-subtitle">欢迎登录，请输入您的账号信息</div>

      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        label-width="80px"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
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

      <div class="login-tips">
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
        >
          <p>• 首次登录请联系管理员获取账号</p>
          <p>• 忘记密码请向管理员申请重置</p>
          <p>• 请妥善保管您的账号信息</p>
        </el-alert>
      </div>
    </el-card>
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
.login-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 450px;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.login-card:hover {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.16), 0 4px 16px rgba(0, 0, 0, 0.08);
}

.login-title {
  font-size: 26px;
  text-align: center;
  margin-bottom: 10px;
  color: #303133;
  font-weight: bold;
}

.login-subtitle {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 30px;
}

.login-form {
  margin-top: 10px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
}

.login-btn-group {
  margin-top: 30px;
  margin-bottom: 10px;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
}

.login-tips {
  margin-top: 20px;
}

.login-tips .el-alert {
  padding: 15px;
}

.login-tips p {
  margin: 5px 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.login-tips p:first-child {
  margin-top: 0;
}

.login-tips p:last-child {
  margin-bottom: 0;
}
</style>
