const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 关键：关闭ESLint严格检查，新手优先保证项目运行
  lintOnSave: false
})