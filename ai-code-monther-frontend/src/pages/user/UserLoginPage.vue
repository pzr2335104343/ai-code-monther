<template>
  <div class="auth-page">
    <div class="auth-card">
      <section class="auth-intro">
        <h2>buling 秒搭</h2>
        <p>用最快的方式把你的创意变成可运行的产品。</p>
        <ul>
          <li>一分钟生成完整前后端项目</li>
          <li>自动部署与在线预览一键完成</li>
          <li>丰富模板与 AI 支持持续更新</li>
        </ul>
      </section>
      <section class="auth-form">
        <header class="form-header">
          <h3>欢迎回来</h3>
          <p>登录账户即可继续创作并查看项目进展</p>
        </header>
        <a-form
          layout="vertical"
          :model="formState"
          name="login"
          autocomplete="off"
          @finish="handleSubmit"
        >
          <a-form-item
            label="账号"
            name="userAccount"
            :rules="[{ required: true, message: '请输入账号' }]"
          >
            <a-input
              v-model:value="formState.userAccount"
              size="large"
              placeholder="请输入账号"
              allow-clear
            />
          </a-form-item>
          <a-form-item
            label="密码"
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码不能小于 8 位' },
            ]"
          >
            <a-input-password
              v-model:value="formState.userPassword"
              size="large"
              placeholder="请输入密码"
            />
          </a-form-item>
          <div class="tips">
            没有账号？
            <RouterLink to="/user/register">立即注册</RouterLink>
          </div>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="submitting"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogin } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const submitting = ref(false)

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserLoginRequest) => {
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    const res = await userLogin(values)
    // 登录成功，把登录态保存到全局状态中
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error(`登录失败，${res.data.message}`)
    }
  } catch (error) {
    message.error('网络异常，请稍后再试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 64px 16px;
  background: linear-gradient(135deg, #f4f7ff 0%, #ffffff 100%);
  min-height: calc(100vh - 120px);
}

.auth-card {
  display: grid;
  grid-template-columns: minmax(0, 320px) minmax(0, 420px);
  gap: 48px;
  padding: 48px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow:
    0 20px 45px rgba(15, 23, 42, 0.12),
    0 2px 10px rgba(15, 23, 42, 0.04);
  width: 100%;
  max-width: 960px;
  box-sizing: border-box;
}

.auth-intro {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
  color: #1d2939;
}

.auth-intro h2 {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #1f1f1f;
}

.auth-intro p {
  font-size: 16px;
  color: #475467;
  margin: 0;
  line-height: 1.6;
}

.auth-intro ul {
  margin: 0;
  padding-left: 20px;
  color: #475467;
  line-height: 1.8;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
}

.form-header p {
  margin: 8px 0 0;
  color: #667085;
  font-size: 14px;
}

.tips {
  display: flex;
  justify-content: flex-end;
  font-size: 13px;
  color: #98a2b3;
  margin-bottom: 4px;
}

.tips a {
  margin-left: 4px;
}

@media (max-width: 960px) {
  .auth-card {
    grid-template-columns: 1fr;
    padding: 32px 24px;
    gap: 32px;
  }

  .auth-intro {
    text-align: center;
    align-items: center;
  }

  .auth-intro ul {
    padding-left: 16px;
    text-align: left;
  }
}
</style>
