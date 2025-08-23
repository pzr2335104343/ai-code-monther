import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 是否已经首次获取过用户信息
  const hasFetchedUser = ref(false)

  // 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
    hasFetchedUser.value = true
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  // 重置首次获取状态（用于登出时）
  function resetFetchStatus() {
    hasFetchedUser.value = false
  }

  return {
    loginUser,
    hasFetchedUser,
    setLoginUser,
    fetchLoginUser,
    resetFetchStatus,
  }
})
