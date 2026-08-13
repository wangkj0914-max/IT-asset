const { defineConfig } = require('@vue/cli-service')
const path = require('path')
const fs = require('fs')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: 'D:\\AI\\dist-v2',
  devServer: {
    host: '0.0.0.0',
    port: 8082,
    https: {
      key: fs.readFileSync(path.resolve(__dirname, 'cert', 'key.pem')),
      cert: fs.readFileSync(path.resolve(__dirname, 'cert', 'cert.pem'))
    },
    historyApiFallback: true,
    proxy: {
      '^/asset/': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
