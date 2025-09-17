<template>
  <div class="dual-version-viewer">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>应用版本代码对比查看器</h1>
    </div>
    <!-- 双面板容器 -->
    <div class="panels-container">
      <!-- 左侧面板 -->
      <div class="version-panel">
        <a-menu mode="horizontal" :items="itemsLeft" @select="handleLeftFileSelect" />
      </div>
      <!-- 右侧面板 -->
      <div class="version-panel">
        <a-menu mode="horizontal" :items="itemsRight" @select="handleRightFileSelect" />
      </div>
    </div>
    <a-card>
      <div>
        <CodeDiff
          :old-string="oldString"
          :new-string="newString"
          output-format="side-by-side" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref,h  } from 'vue'
import { CodeDiff } from 'v-code-diff'
import {  SettingOutlined } from '@ant-design/icons-vue'
import { serveStaticResource } from '@/api/staticResourceController.js'
import { MenuProps, message } from 'ant-design-vue'
import { useRoute } from 'vue-router'

let oldString = ref('12233')
let newString = ref('1234')
const route = useRoute()
// 版本数据
const itemsLeft = ref<MenuProps['items']>([
  {
    key: 'sub1',
    icon: () => h(SettingOutlined),
    label: '左侧数据',
    title: '左侧数据',
    children: [
      {
        type: 'group',
        label: 'v1',
        children: [
          {
            label: 'index.html',
            key: 'v1/index.html',
          },
          {
            label: 'style.css',
            key: 'v1/style.css',
          },
        ],
      }
    ],
  },
])
const itemsRight = ref<MenuProps['items']>([
  {
    key: 'sub1',
    icon: () => h(SettingOutlined),
    label: '右侧数据',
    title: '右侧数据',
    children: [
      {
        type: 'group',
        label: 'v1',
        children: [
          {
            label: 'index.html',
            key: 'v1/index.html',
          },
          {
            label: 'style.css',
            key: 'v1/style.css',
          },
        ],
      }
    ],
  },
])

// 处理左侧文件选择事件
function handleLeftFileSelect(item) {
  getFile('left', item.selectedKeys[0])
}

// 处理右侧文件选择事件
function handleRightFileSelect(item) {
  getFile('right', item.selectedKeys[0])
}

// 文件获取函数（需根据实际需求实现）
async function getFile(box: string, pathName: string) {
  // 实际实现逻辑：
  // 1. 调用后端接口获取文件内容
  const res = await serveStaticResource({
    deployKey: route.params.deployKey as string,
    appVersion: pathName
  })
  // 2. 处理返回的文件内容（如展示、对比等）
  if (res.data) {
    if (box === 'left') {
      oldString.value = res.data
    } else {
      newString.value = res.data
    }
  } else {
    message.error('获取文件内容失败')
  }
}
</script>

<style scoped>
.dual-version-viewer {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  color: rgba(0, 0, 0, 0.85);
}

.panels-container {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 3vh;
}

.version-panel {
  overflow: auto;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .panels-container {
    flex-direction: column;
    height: auto;
  }

  .version-panel {
    height: 400px;
  }
}
</style>
