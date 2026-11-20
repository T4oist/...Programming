<template>
  <div class="page-container">
    <div class="promo-card" v-loading="loading">
      <h2 style="margin-top: 0">活动签到</h2>
      <p class="promo-meta">{{ activity.title || '加载中…' }}</p>
      <el-tag type="success">动态二维码签到</el-tag>

      <!-- 无凭证：二维码链接不完整或已失效 -->
      <el-result v-if="!token" icon="warning" title="签到入口无效"
                 sub-title="请扫描管理端展示的最新签到二维码后再签到">
        <template #extra>
          <el-button @click="$router.push(`/a/${id}`)">查看活动详情</el-button>
        </template>
      </el-result>

      <!-- 活动加载失败 -->
      <el-result v-else-if="loadError" icon="warning" title="活动信息加载失败"
                 sub-title="请检查网络后刷新页面重试">
        <template #extra>
          <el-button @click="$router.go(0)">重新加载</el-button>
        </template>
      </el-result>

      <!-- 签到成功 -->
      <el-result v-else-if="result.checkedIn" icon="success"
                 :title="result.duplicated ? '您已签到' : '签到成功'"
                 :sub-title="result.message">
        <template #extra>
          <el-button @click="$router.push(`/a/${id}`)">查看活动详情</el-button>
        </template>
      </el-result>

      <el-form v-else :model="form" label-position="top" style="margin-top: 20px">
        <el-form-item label="报名手机号">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入报名时填写的手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" size="large" :loading="submitting"
                     @click="onSubmit">确认签到</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicActivity, checkin } from '../../api/modules'

const route = useRoute()
const id = route.params.id
const activity = reactive({})
const form = reactive({ phone: '' })
const result = ref({})
const submitting = ref(false)
const loading = ref(true)
const loadError = ref(false)

// 动态签到凭证从二维码 URL 读取后存入内存，并从地址栏清除，避免进入历史/日志/Referer
const token = route.query.token || ''
if (token) {
  window.history.replaceState({}, document.title, route.path)
}

onMounted(async () => {
  try {
    Object.assign(activity, await getPublicActivity(id))
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  if (!token) {
    ElMessage.warning('签到入口无效，请重新扫码')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  submitting.value = true
  try {
    result.value = await checkin({ activityId: Number(id), phone: form.phone, token })
  } finally {
    submitting.value = false
  }
}
</script>
