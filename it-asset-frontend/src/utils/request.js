import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/asset', // 通过 Vue devServer 代理，无需跨域
  timeout: 10000 // 请求超时时间
})

// 清理本地登录态
const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
}

/**
 * 统一的 401/403 未授权处理。
 * 关键：区分「本地本来就没有 token」与「本地有 token 但被拒」两种情况——
 * - 无 token（如登录页上的轮询/无谓请求）：静默忽略，不弹提示、不跳转，直接 reject；
 * - 有 token 但被拒（正常登录过期/无权限）：提示、清理登录态并跳转登录页。
 * 注意：判断是否持有 token 必须在清理 localStorage 之前读取。
 * @param {string} [message] 有 token 时展示的提示文案，缺省使用「登录已过期或无权限，请重新登录！」
 * @returns {boolean} true 表示有 token（已提示并处理），false 表示本来无 token（已静默忽略）
 */
const handleUnauthorized = (message) => {
  const hadToken = !!localStorage.getItem('token')
  if (!hadToken) {
    // 未登录状态下的无谓请求：静默拒绝，避免在登录页弹出「登录已过期」误导提示
    return false
  }
  ElMessage.error(message || '登录已过期或无权限，请重新登录！')
  clearAuth()
  if (router.currentRoute.value.path !== '/') {
    router.push('/')
  }
  return true
}

// 请求拦截器：添加 token + 站点
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    // 全局站点参数
    const site = localStorage.getItem('site')
    if (site) {
      try { config.headers['X-Site'] = encodeURIComponent(site) }
      catch (e) { config.headers['X-Site'] = site }
      // 所有请求都加 query param，确保后端 @RequestParam 能读到
      config.params = config.params || {}
      if (!config.params.site) config.params.site = site
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 检查后端返回的状态码
    if (res.code !== 200) {
      // 401/403 未授权：统一走 handleUnauthorized（无 token 静默忽略，有 token 才提示并跳登录页）
      if (res.code === 401 || res.code === 403) {
        handleUnauthorized(res.msg)
      } else {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    return res
  },
  error => {
    // HTTP 错误
    if (error.response?.status === 401 || error.response?.status === 403) {
      // 统一未授权处理：登录页等无 token 场景静默忽略，正常过期场景仍提示并跳登录页
      handleUnauthorized()
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
