<template>
  <div class="page-container">
    <div class="header-title"><span>修改密码</span></div>
    <div class="table-section" style="max-width:420px;margin:0 auto">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="username" disabled /></el-form-item>
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" placeholder="请输入原密码" /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" placeholder="至少6位" /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submit" :loading="submitting">确认修改</el-button></el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateSame = (rule, value, callback) => {
  if (value !== form.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const rules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateSame, trigger: 'blur' }]
})

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    const r = await request.post('/user/change-password', { ...form, userId: localStorage.getItem('userId') })
    if (r.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.clear()
      setTimeout(() => router.push('/'), 1000)
    } else {
      ElMessage.error(r.msg || '修改失败')
    }
  } catch (e) { /* validation */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.page-container{width:95%;margin:0 auto;padding:20px}
.header-title{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:16px 24px;font-size:18px;font-weight:bold;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 12px rgba(102,126,234,0.3)}
.table-section{background:white;padding:20px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.05)}
</style>
