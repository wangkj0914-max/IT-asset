const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: 'D:\\AI\\dist-v2',
  chainWebpack: config => {
    // 禁用babel-loader缓存，避免沙箱权限问题
    config.module.rule('js').use('babel-loader').tap(options => {
      options = options || {}
      options.cacheDirectory = false
      return options
    })
    // 禁用eslint缓存
    config.plugin('eslint').tap(args => {
      if (args[0]) args[0].cache = false
      return args
    })
  },
  devServer: {
    historyApiFallback: true,
    proxy: {
      '^/asset/': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})


