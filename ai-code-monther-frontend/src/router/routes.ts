import UserManagerPage from '@/pages/admin/UserManagerPage.vue'
import HomeView from '@/pages/HomeView.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import ACCESS_ENUM from '@/access/accessEnum.ts'

const routes = [
  {
    path: '/',
    name: '主页',
    component: HomeView,
    meta: {
      requireAuth: ACCESS_ENUM.NOT_LOGIN,
      icon: HomeOutlined,
    },
  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
    meta: {
      requireAuth: ACCESS_ENUM.NOT_LOGIN,
      hideInMenu: true,
    },
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
    meta: {
      requireAuth: ACCESS_ENUM.NOT_LOGIN,
      hideInMenu: true,
    },
  },
  {
    path: '/admin/userManager',
    name: '用户管理',
    component: UserManagerPage,
    meta: {
      requireAuth: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: '/notFound',
    name: '404',
    component: () => import(/* webpackChunkName: "notfound" */ '@/pages/NoAuth.vue'),
    meta: {
      requireAuth: ACCESS_ENUM.NOT_LOGIN,
      hideInMenu: true,
    },
  },
]
export default routes
