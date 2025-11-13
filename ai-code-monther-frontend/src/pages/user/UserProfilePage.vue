<template>
  <div id="userProfilePage">
    <h2 class="title">修改个人信息</h2>
    <a-form :model="formState" :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" @finish="onSubmit">
      <a-form-item label="名字" name="userName" :rules="[{ required: true, message: '请输入名字' }]">
        <a-input v-model:value="formState.userName" placeholder="请输入名字" />
      </a-form-item>
      <a-form-item label="上传头像">
        <a-upload
          v-model:file-list="fileList"
          name="avatar"
          list-type="picture-card"
          class="avatar-uploader"
          :before-upload="handleAvatarUpload"
          :disabled="uploading"
          accept="image/*"
          @change="handleChange"
        >
          <div v-if="fileList.length === 0">
            <PlusOutlined />
            <div class="ant-upload-text">{{ uploading ? '上传中...' : '上传头像' }}</div>
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="头像地址" name="userAvatar" :rules="[{ required: true, message: '请输入头像链接' }]">
        <a-input v-model:value="formState.userAvatar" placeholder="请输入头像图片链接" />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" :loading="saving">保存</a-button>
          <a-button @click="resetForm">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadFile, UploadProps } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { updateUser, uploadAvatar } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const saving = ref(false)
const uploading = ref(false)
const fileList = ref<UploadFile[]>([])

const formState = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
})

const loadForm = () => {
  const u = loginUserStore.loginUser
  formState.id = u.id
  formState.userName = u.userName || ''
  formState.userAvatar = u.userAvatar || ''
  if (formState.userAvatar) {
    fileList.value = [
      {
        uid: '-1',
        name: 'avatar.png',
        status: 'done',
        url: formState.userAvatar,
      },
    ]
  } else {
    fileList.value = []
  }
}

onMounted(() => {
  loadForm()
})

const onSubmit = async () => {
  if (!formState.id) {
    message.error('请先登录')
    return
  }
  saving.value = true
  try {
    const res = await updateUser({
      id: formState.id,
      userName: formState.userName,
      userAvatar: formState.userAvatar,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      await loginUserStore.fetchLoginUser()
      loadForm()
    } else {
      message.error('保存失败，' + res.data.message)
    }
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  loadForm()
}

const handleAvatarUpload: UploadProps['beforeUpload'] = async (file) => {
  const uploadFile = file as UploadFile
  const rawFile = uploadFile.originFileObj ?? (file as unknown as File)
  if (!(rawFile instanceof File)) {
    message.error('未获取到文件，请重试')
    return false
  }
  const formData = new FormData()
  formData.append('avatar', rawFile as Blob, rawFile.name)
  uploading.value = true
  try {
    const res = await uploadAvatar(formData)
    if (res.data.code === 0) {
      formState.userAvatar = res.data.data ?? ''
      fileList.value = [
        {
          uid: String(Date.now()),
          name: rawFile.name,
          status: 'done',
          url: formState.userAvatar,
        },
      ]
      message.success('头像上传成功')
    } else {
      message.error(`头像上传失败，${res.data.message}`)
    }
  } catch {
    message.error('头像上传出现异常，请稍后重试')
  } finally {
    uploading.value = false
  }
  return false
}

const handleChange = (info: UploadChangeParam<UploadFile>) => {
  fileList.value = info.fileList.slice(-1)
  if (info.file && info.file.status === 'removed') {
    formState.userAvatar = ''
  }
}

watch(
  () => formState.userAvatar,
  (val) => {
    if (val) {
      fileList.value = [
        {
          uid: '-1',
          name: fileList.value[0]?.name ?? 'avatar.png',
          status: 'done',
          url: val,
        },
      ]
    } else {
      fileList.value = []
    }
  },
)
</script>
<style scoped>
#userProfilePage {
  padding: 24px;
  background: #ffffff;
  max-width: 800px;
  margin: 16px auto;
  border-radius: 12px;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.avatar-uploader :deep(.ant-upload),
.avatar-uploader :deep(.ant-upload-select-picture-card),
.avatar-uploader :deep(.ant-upload-list-picture-card-container),
.avatar-uploader :deep(.ant-upload-list-item) {
  width: 100px;
  height: 100px;
}
.avatar-uploader :deep(.ant-upload) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

