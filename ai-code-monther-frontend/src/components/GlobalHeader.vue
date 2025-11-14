<template>
  <a-layout-header class="header" :class="{ 'is-scrolled': scrolled }">
    <div class="header-inner">
      <a-row :wrap="false" justify="start" align="middle">
      <!-- 左侧：Logo和标题 -->
        <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <div class="logo-wrap">
              <img class="logo" src="@/assets/logo-warm.svg" alt="Logo" />
            </div>
            <h1 class="site-title">buling 秒搭</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
        <a-col flex="auto" class="menu-col">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
            :overflowed-indicator="null"
            :disabledOverflow="true"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
        <a-col flex="220px" class="user-col">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="goProfile">
                    修改个人信息
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
      </a-row>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  }
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 跳转到个人信息页
const goProfile = () => {
  router.push('/user/profile')
}
// 吸顶与滚动收缩
const scrolled = ref(false)
const onScroll = () => {
  scrolled.value = window.scrollY > 12
}
onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.header {
  /* 纯白背景，去除暖色渐变 */
  background: #ffffff;
  padding: 8px 24px;
  box-shadow: none;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  transition: padding 220ms ease, box-shadow 220ms ease, background-color 220ms ease;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-wrap {
  position: relative;
  display: grid;
  place-items: center;
  height: 48px;
  width: 48px;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(255, 138, 91, 0.18);
  transition: transform 200ms ease, box-shadow 220ms ease;
}
.logo-wrap:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(255, 111, 97, 0.22);
}
.logo {
  height: 100%;
  width: 100%;
}

.site-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.2px;
  background: linear-gradient(135deg, #ffb86c, #ff8a5b 50%, #ff6f61);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* 导航菜单圆润与无分割线 */
.ant-menu-horizontal {
  border-bottom: none !important;
  background: transparent;
}

.menu-col {
  display: flex;
  justify-content: flex-start;
  overflow-x: auto;
  scrollbar-width: none; /* Firefox */
}
.menu-col :deep(.ant-menu) {
  margin: 0;
  border-radius: 12px;
  padding: 0 6px;
  white-space: nowrap;
  width: max-content;
}
.menu-col :deep(.ant-menu-overflow) { overflow: visible; }
.menu-col :deep(.ant-menu-submenu-overflowed) { display: none !important; }
.menu-col :deep(.ant-menu-item),
.menu-col :deep(.ant-menu-submenu) {
  padding: 0 14px;
  height: 56px;
  line-height: 56px;
  background: transparent !important;
}
.menu-col::-webkit-scrollbar { display: none; } /* Chrome/Safari */

.user-col {
  display: flex;
  justify-content: flex-end;
  margin-left: auto;
}

/* 文本导航风格：无底色，使用下划线强调 */
:deep(.ant-menu-horizontal > .ant-menu-item) {
  position: relative;
  color: #6b7280; /* 中性文本色 */
  transition: color 200ms ease;
}
:deep(.ant-menu-horizontal > .ant-menu-item:hover) {
  color: #cc5a3f; /* 暖色文字 */
}
:deep(.ant-menu-horizontal > .ant-menu-item::after) {
  content: '';
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 8px;
  height: 2px;
  background: transparent;
  border-radius: 2px;
  transition: background-color 200ms ease, transform 200ms ease, opacity 200ms ease;
  transform: scaleX(0.6);
  opacity: 0;
}
:deep(.ant-menu-horizontal > .ant-menu-item:hover::after) {
  background: rgba(255, 184, 108, 0.45);
  transform: scaleX(1);
  opacity: 1;
}
:deep(.ant-menu-horizontal > .ant-menu-item-selected) {
  color: #cc5a3f !important;
  font-weight: 600;
}
:deep(.ant-menu-horizontal > .ant-menu-item-selected::after) {
  background: linear-gradient(90deg, #ffb86c, #ff8a5b 60%, #ff6f61);
  transform: scaleX(1);
  opacity: 1;
}

/* 顶部右侧用户区按钮更圆润 */
:deep(.ant-btn) {
  border-radius: 12px;
}

/* 滚动后的收缩与阴影增强 */
.is-scrolled {
  padding: 2px 24px;
  background: #ffffff;
  box-shadow: none;
}
.is-scrolled .logo-wrap {
  /* 保持 Logo 尺寸不变，仅微调阴影 */
  box-shadow: 0 6px 16px rgba(255, 138, 91, 0.22);
}
.is-scrolled .site-title {
  /* 标题字号保持不变，提高一致性 */
  font-size: 20px;
}
</style>
