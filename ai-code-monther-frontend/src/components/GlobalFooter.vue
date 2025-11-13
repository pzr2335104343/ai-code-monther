<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        &copy; {{ new Date().getFullYear() }} buling 秒搭
      </p>
      <div class="flex flex-wrap justify-center gap-4 md:gap-6">
        <!-- ICP备案 -->
        <a
          href="https://beian.miit.gov.cn/"
          target="_blank"
          class="hover:text-blue-600 transition-colors duration-300 flex items-center"
        >
          <i class="fa fa-shield mr-1"></i> 苏ICP备2024099995号-3
        </a>
      </div>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  /* 纯白底色，去除暖色渐变 */
  background: #ffffff;
  text-align: center;
  padding: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
