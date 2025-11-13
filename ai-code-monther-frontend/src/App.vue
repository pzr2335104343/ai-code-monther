<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#6b8cff',
        colorInfo: '#6b8cff',
        colorBgBase: '#ffffff',
        colorBgContainer: '#ffffff',
        colorText: '#1f2430',
        colorTextSecondary: '#5b6474',
        borderRadius: 12,
        borderRadiusLG: 14,
        borderRadiusSM: 10,
        boxShadow: '0 6px 24px rgba(32,54,93,0.08), 0 2px 8px rgba(32,54,93,0.06)'
      }
    }"
  >
    <BasicLayout />
  </a-config-provider>
</template>

<style>
#app {
  min-height: 100vh;
}
</style>
