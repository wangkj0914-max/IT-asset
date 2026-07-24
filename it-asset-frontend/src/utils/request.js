import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/asset', // 通过 Vue devServer 代理，无需跨域
  timeout: 10000 // 请求超时时间
})

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
      ElMessage.error(res.msg || '请求失败')
      // 401 未授权，跳转到登录页
      if (res.code === 401 || res.code === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('realName')
        localStorage.removeItem('role')
        localStorage.removeItem('userId')
        if (router.currentRoute.value.path !== '/') {
          router.push('/')
        }
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    return res
  },
  error => {
    // HTTP 错误
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或无权限，请重新登录！')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('role')
      localStorage.removeItem('userId')
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      }
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
