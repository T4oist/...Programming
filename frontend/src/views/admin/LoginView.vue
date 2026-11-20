<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 24px">高校学生活动全流程管理系统</h2>
      <el-form :model="form" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">
          登 录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/modules'
import { reset401Flag } from '../../api/index'
import { safeRedirect } from '../../utils/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(form)
    reset401Flag()
    localStorage.setItem('token', data.token)
    localStorage.setItem('adminName', data.realName)
    ElMessage.success('登录成功')
    router.push(safeRedirect(route.query.redirect))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff33, #f5f7fa);
}

.login-card {
  width: 400px;
}
</style>
