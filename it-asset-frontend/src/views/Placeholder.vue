<template>
  <div class="placeholder-container">
    <el-empty :description="t('ui.功能开发中')" />
  </div>
</template>

<style scoped>
.placeholder-container {
  padding: 100px 20px;
  text-align: center;
}
</style>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>
