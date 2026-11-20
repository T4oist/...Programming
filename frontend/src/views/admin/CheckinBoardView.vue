<template>
  <el-card style="max-width: 720px; margin: 0 auto">
    <template #header>
      <div class="card-header">
        <span>现场签到二维码 — {{ tokenData.title }}</span>
        <el-button link @click="$router.back()">返回详情</el-button>
      </div>
    </template>

    <div class="qr-wrap" v-loading="loading">
      <img v-if="qrImage" :src="qrImage" alt="动态签到二维码" width="280" height="280" />
      <div class="qr-error" v-else-if="qrError">
        <p>二维码生成失败，请检查网络后重试</p>
        <el-button type="primary" size="small" @click="refresh">重新生成</el-button>
      </div>
      <div class="qr-placeholder" v-else>二维码生成中…</div>
      <div class="hint">
        <p>动态二维码每 {{ tokenData.refreshSeconds || 30 }} 秒自动刷新，旧码过期后无法签到。</p>
        <p>参与人扫码后输入报名手机号完成签到；距下次刷新约 <b>{{ countdown }}s</b>。</p>
        <p>签到窗口：活动开始前 30 分钟至活动结束。</p>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import QRCode from 'qrcode'
import { getCheckinToken } from '../../api/modules'

const id = useRoute().params.id
const tokenData = ref({})
const qrImage = ref('')
const qrError = ref(false)
const loading = ref(false)
const countdown = ref(30)
let refreshTimer = null
let tickTimer = null

function scheduleRefresh(seconds) {
  clearInterval(refreshTimer)
  // 用后端返回的刷新周期驱动，至少留 5 秒缓冲，避免与过期时间完全重合
  refreshTimer = setInterval(refresh, Math.max(seconds || 30, 5) * 1000)
}

async function refresh() {
  loading.value = true
  qrError.value = false
  try {
    const data = await getCheckinToken(id)
    tokenData.value = data
    // 不依赖后端返回的绝对地址（可能写死 localhost），用当前站点同源重建，保证扫码可用
    const url = `${location.origin}/checkin/${id}?token=${encodeURIComponent(data.token)}`
    qrImage.value = await QRCode.toDataURL(url, {
      width: 280,
      margin: 1,
      color: { dark: '#000000', light: '#ffffff' }
    })
    // 按后端到期时间校准倒计时，避免本地累计误差
    countdown.value = Math.max(0, Math.ceil((data.expireAt - Date.now()) / 1000)) || (data.refreshSeconds || 30)
    scheduleRefresh(data.refreshSeconds || 30)
  } catch (e) {
    qrImage.value = ''
    qrError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
  tickTimer = setInterval(() => {
    countdown.value = countdown.value > 0 ? countdown.value - 1 : 0
  }, 1000)
})

onUnmounted(() => {
  clearInterval(refreshTimer)
  clearInterval(tickTimer)
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.qr-wrap { text-align: center; }
.qr-placeholder, .qr-error {
  width: 280px;
  min-height: 280px;
  box-sizing: border-box;
  border: 1px dashed #ccc;
  margin: 0 auto;
  padding: 100px 10px;
  color: #999;
}
.hint {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}
</style>
