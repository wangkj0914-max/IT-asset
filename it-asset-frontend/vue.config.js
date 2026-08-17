const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: 'dist',
  chainWebpack: config => {
    config.module.rule('js').use('babel-loader').tap(options => {
      options = options || {}
      options.cacheDirectory = false
      return options
    })
    config.plugin('eslint').tap(args => {
      if (args[0]) args[0].cache = false
      return args
    })
  },
  devServer: {
    host: '0.0.0.0',
    port: 8081,
    historyApiFallback: true,
    proxy: {
      '^/asset/': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})


