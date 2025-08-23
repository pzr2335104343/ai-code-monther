<template>
  <a-layout-header class="global-header">
    <div class="header-container">
      <!-- 左侧 Logo 和标题 -->
      <div class="header-left">
        <div class="logo-container">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
          <h1 class="site-title">AI Code Mother</h1>
        </div>
        <!-- 菜单 -->
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          class="header-menu"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧用户信息 -->
      <div class="header-right">
        <div v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a-space>
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名' }}
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <a-space> <LogoutOutlined />退出登录 </a-space>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <a-button v-else type="primary" @click="handleLogin">
          <template #icon>
            <UserOutlined />
          </template>
          登录
        </a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import routes from '@/router/routes.ts'
import checkAccess from '@/access/checkAccess.ts'

const router = useRouter()
const selectedKeys = ref<string[]>(['home'])
const loginUserStore = useLoginUserStore()
// 菜单配置项 - 直接根据权限过滤和转换
const menuItems = computed(() =>
  routes
    .filter(
      (item) =>
        !item.meta?.hideInMenu && checkAccess(loginUserStore.loginUser, item.meta?.requireAuth),
    )
    .map((item) => ({
      key: item.path as string,
      label: item.name as string,
      path: item.path as string,
      icon: () => item.meta?.icon,
    })),
)
// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = menuItems.value?.find((item) => item?.key === key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

const handleLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    loginUserStore.resetFetchStatus() // 重置首次获取状态
    message.success('注销成功')
    router.push('/user/login')
  } else {
    message.error('注销失败')
  }
}

// 登录处理
const handleLogin = () => {
  router.push('/user/login')
}

// 监听路由变化
router.afterEach((to) => {
  selectedKeys.value = [to.name as string]
})
</script>

<style scoped>
.global-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  line-height: 64px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  width: 90%;
  align-items: center;
  gap: 48px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 32px;
  height: 32px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-menu {
  border: none;
  width: 70%;
  background: transparent;
  line-height: 64px;
}

.header-menu :deep(.ant-menu-item) {
  height: 64px;
  line-height: 64px;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .header-left {
    gap: 24px;
  }

  .site-title {
    font-size: 16px;
  }

  .header-menu {
    display: none; /* 移动端隐藏菜单，可以考虑使用抽屉菜单 */
  }
}

@media (max-width: 480px) {
  .site-title {
    display: none; /* 超小屏幕隐藏标题 */
  }
}
</style>
