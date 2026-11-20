<template>
  <el-card style="max-width: 680px">
    <template #header>个人中心</template>
    <el-tabs>
      <el-tab-pane label="个人信息">
        <el-form :model="profile" label-width="100px" style="max-width: 460px">
          <el-form-item label="账号">
            <el-input v-model="profile.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profile.realName" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profile.phone" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码">
        <el-form :model="pwd" label-width="100px" style="max-width: 460px">
          <el-form-item label="原密码">
            <el-input v-model="pwd.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwd.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingPwd" @click="savePassword">确认修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '../../api/modules'
import { setAdminName } from '../../utils/auth'

const profile = reactive({ username: '', realName: '', phone: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const savingProfile = ref(false)
const savingPwd = ref(false)

onMounted(async () => {
  try {
    Object.assign(profile, await getProfile())
  } catch (e) {
    ElMessage.error('个人信息加载失败')
  }
})

async function saveProfile() {
  if (savingProfile.value) return
  savingProfile.value = true
  try {
    const data = await updateProfile({ realName: profile.realName, phone: profile.phone })
    Object.assign(profile, data)
    setAdminName(data.realName) // 顶部布局实时同步
    ElMessage.success('个人信息已更新')
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  if (savingPwd.value) return
  savingPwd.value = true
  try {
    await changePassword(pwd)
    pwd.oldPassword = ''
    pwd.newPassword = ''
    ElMessage.success('密码已修改，请使用新密码重新登录')
  } finally {
    savingPwd.value = false
  }
}
</script>
