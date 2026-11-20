<template>
  <div class="page-container">
    <div class="promo-card" v-if="loadError">
      <el-result icon="warning" title="活动信息加载失败" sub-title="请检查网络后刷新页面重试">
        <template #extra>
          <el-button @click="$router.go(0)">重新加载</el-button>
        </template>
      </el-result>
    </div>
    <div class="promo-card" v-else-if="activity.id">
      <el-tag :type="activity.published ? 'success' : 'info'">{{ activity.statusLabel }}</el-tag>
      <h1 class="promo-title" style="margin-top: 12px">{{ activity.title }}</h1>
      <div class="promo-meta">
        <div>活动类型：{{ activity.type }}</div>
        <div>活动时间：{{ activity.startTime }} 至 {{ activity.endTime }}</div>
        <div>活动地点：{{ activity.location }}</div>
        <div>报名截止：{{ activity.signupDeadline }}</div>
        <div>人数上限：{{ activity.capacity }} 人 · 已报名 {{ activity.enrolledCount }} 人</div>
      </div>
      <div class="promo-intro">{{ activity.intro }}</div>

      <div class="qr-area" v-if="qrImage">
        <img :src="qrImage" alt="报名二维码" width="180" height="180" />
        <div>扫码报名</div>
      </div>

      <div style="text-align: center; margin-top: 20px">
        <el-button
          type="primary"
          size="large"
          :loading="!qrImage"
          :disabled="!activity.signupAvailable"
          @click="$router.push(`/a/${activity.id}/signup`)"
        >
          {{ activity.signupAvailable ? '立即报名' : '报名已结束' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import QRCode from 'qrcode'
import { getPublicActivity } from '../../api/modules'

const id = useRoute().params.id
const activity = reactive({})
const qrImage = ref('')
const loadError = ref(false)

onMounted(async () => {
  try {
    Object.assign(activity, await getPublicActivity(id))
    // 报名二维码指向固定报名地址，扫码即可打开本活动的报名页
    const url = `${location.origin}/a/${id}/signup`
    qrImage.value = await QRCode.toDataURL(url, { width: 180, margin: 1 })
  } catch (e) {
    loadError.value = true
  }
})
</script>

<style scoped>
.qr-area {
  text-align: center;
  margin-top: 20px;
  color: #606266;
  font-size: 13px;
}
</style>
