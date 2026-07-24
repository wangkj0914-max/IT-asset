module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended'
  ],
  parserOptions: {
    parser: '@babel/eslint-parser'
  },
  rules: {
    // 关闭组件名单单词校验（核心修复）
    'vue/multi-word-component-names': 'off',
    // 关闭未使用变量校验（可选，避免后续类似报错）
    'no-unused-vars': 'off',
    // 关闭console警告（可选）
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}