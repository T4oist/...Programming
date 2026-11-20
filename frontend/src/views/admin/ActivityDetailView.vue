<template>
  <div v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动详情 — {{ detail.title }}</span>
          <div>
            <el-button type="primary" @click="publishNow">
              {{ detail.published ? '重新生成宣传页' : '发布活动' }}
            </el-button>
            <el-button type="warning" @click="$router.push(`/admin/checkin/${detail.id}`)">
              签到二维码
            </el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusLabel }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.type }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="报名截止">{{ detail.signupDeadline }}</el-descriptions-item>
        <el-descriptions-item label="人数上限">{{ detail.capacity }}</el-descriptions-item>
        <el-descriptions-item label="已报名">{{ detail.enrolledCount }}</el-descriptions-item>
        <el-descriptions-item label="首次发布">{{ detail.firstPublishedAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="活动简介" :span="2">{{ detail.intro }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail.published" class="share-box">
        <el-alert
          v-if="isLocalhost"
          type="warning"
          :closable="false"
          show-icon
          title="当前通过 localhost 访问，分享链接和二维码只能在本机打开；请用电脑局域网 IP 访问本页后再分享。"
          style="margin-bottom: 8px"
        />
        <div>宣传页分享链接（报名地址稳定，编辑后不变）：</div>
        <el-link type="primary" :href="shareLink" target="_blank" rel="noopener noreferrer">{{ shareLink }}</el-link>
        <div style="margin-top: 4px">
          报名页直达链接：
          <el-link type="primary" :href="signupLink" target="_blank" rel="noopener noreferrer">{{ signupLink }}</el-link>
        </div>
        <div style="margin-top: 4px">现场签到页：{{ checkinLink }}</div>
      </div>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <span>报名与签到统计</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="8"><el-statistic title="已报名" :value="stats.enrolledCount || 0" /></el-col>
        <el-col :span="8"><el-statistic title="已签到" :value="stats.checkedCount || 0" /></el-col>
        <el-col :span="8"><el-statistic title="签到率" :value="stats.checkinRate || '0%'" /></el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>
        <span>报名名单（{{ enrollments.length }} 人）</span>
      </template>
      <el-table :data="enrollments" border style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="college" label="学院" />
        <el-table-column prop="createdAt" label="报名时间" width="180" />
        <el-table-column label="签到状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.checkedIn ? 'success' : 'info'">
              {{ row.checkedIn ? '已签到' : '未签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkedAt" label="签到时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivity, getEnrollments, getStats, publishActivity } from '../../api/modules'

const id = useRoute().params.id
const detail = reactive({})
const enrollments = ref([])
const stats = reactive({})
const loading = ref(true)

// 链接统一按“当前访问地址”生成：管理员用局域网 IP / 域名打开，分享出去的就是该地址，
// 不再依赖后端 public-base-url，换 IP、换域名都无需改配置。
const shareLink = computed(() => `${location.origin}/a/${id}`)
const signupLink = computed(() => `${location.origin}/a/${id}/signup`)
const checkinLink = computed(() => `${location.origin}/checkin/${id}`)
const isLocalhost = ['localhost', '127.0.0.1', '[::1]'].includes(location.hostname)

async function load() {
  loading.value = true
  try {
    // 三个请求无依赖，并行发起，缩短首屏等待
    const [act, list, st] = await Promise.all([
      getActivity(id), getEnrollments(id), getStats(id)
    ])
    Object.assign(detail, act)
    enrollments.value = list
    Object.assign(stats, st)
  } finally {
    loading.value = false
  }
}

async function publishNow() {
  try {
    await ElMessageBox.confirm(
      detail.published ? '重新生成宣传页？已有报名记录保持不变。' : '确认发布该活动？',
      '提示', { type: 'info' }
    )
    Object.assign(detail, await publishActivity(id))
    ElMessage.success('操作成功，宣传页已生成')
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.share-box {
  margin-top: 16px;
  padding: 12px;
  background: #f4f4f5;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.9;
}
</style>
