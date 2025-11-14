<template>
  <div class="auth-page">
    <div class="auth-card">
      <section class="auth-intro">
        <h2>buling 秒搭</h2>
        <p>注册一个账户，开启智能搭建应用的新体验。</p>
        <ul>
          <li>AI 驱动的应用搭建流程</li>
          <li>预置行业模板，快速上线业务</li>
          <li>团队协作与版本管理一步到位</li>
        </ul>
      </section>
      <section class="auth-form">
        <header class="form-header">
          <h3>创建新账户</h3>
          <p>填写信息即可完成注册，快来加入我们的创作者社区</p>
        </header>
        <a-form
          layout="vertical"
          :model="formState"
          name="register"
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
          <a-form-item
            label="确认密码"
            name="checkPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { min: 8, message: '密码不能小于 8 位' },
              { validator: validateCheckPassword },
            ]"
          >
            <a-input-password
              v-model:value="formState.checkPassword"
              size="large"
              placeholder="请再次输入密码"
            />
          </a-form-item>
          <div class="tips">
            已有账号？
            <RouterLink to="/user/login">立即登录</RouterLink>
          </div>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="submitting"
            >
              注册
            </a-button>
          </a-form-item>
        </a-form>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const submitting = ref(false)

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    const res = await userRegister(values)
    // 注册成功，跳转到登录页面
    if (res.data.code === 0) {
      message.success('注册成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error(`注册失败，${res.data.message}`)
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
