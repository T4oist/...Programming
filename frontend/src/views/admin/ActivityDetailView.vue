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

      <div v-if="detail.shareUrl" class="share-box">
        <div>宣传页分享链接（报名地址稳定，编辑后不变）：</div>
        <el-link type="primary" :href="detail.shareUrl" target="_blank" rel="noopener noreferrer">{{ detail.shareUrl }}</el-link>
        <div style="margin-top: 4px">现场签到页：{{ detail.checkinUrl }}</div>
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivity, getEnrollments, getStats, publishActivity } from '../../api/modules'

const id = useRoute().params.id
const detail = reactive({})
const enrollments = ref([])
const stats = reactive({})
const loading = ref(true)

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
