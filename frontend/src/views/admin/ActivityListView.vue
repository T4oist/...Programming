<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <el-button type="primary" @click="$router.push('/admin/activities/new')">
            新建活动
          </el-button>
        </div>
      </template>

      <!-- 多条件筛选：类型 / 状态 / 开始时间范围 -->
      <el-form :inline="true" :model="query" size="default">
        <el-form-item label="活动类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 130px">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="未发布" value="DRAFT" />
            <el-option label="报名中" value="SIGNUP_OPEN" />
            <el-option label="已截止" value="CLOSED" />
            <el-option label="已结束" value="FINISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="活动名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" v-loading="loading" border style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="200" />
        <el-table-column prop="type" label="类型" width="110" />
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="首次发布时间" width="170">
          <template #default="{ row }">{{ row.firstPublishedAt || '—' }}</template>
        </el-table-column>
        <el-table-column label="已报名" width="100">
          <template #default="{ row }">{{ row.enrolledCount }} / {{ row.capacity }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/admin/activities/${row.id}`)">
              详情
            </el-button>
            <el-button link type="primary" @click="$router.push(`/admin/activities/${row.id}/edit`)">
              编辑
            </el-button>
            <el-button link type="success" @click="publish(row)">
              {{ row.published ? '重新生成宣传页' : '发布' }}
            </el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listActivities, publishActivity, deleteActivity } from '../../api/modules'

const types = ['讲座', '宣讲会', '就业指导', '其他']
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const timeRange = ref([])
const query = reactive({
  type: '', status: '', keyword: '', pageNum: 1, pageSize: 10
})

function statusLabel(s) {
  return { DRAFT: '未发布', SIGNUP_OPEN: '报名中', CLOSED: '已截止', FINISHED: '已结束' }[s] || s
}
function statusTag(s) {
  return { DRAFT: 'info', SIGNUP_OPEN: 'success', CLOSED: 'warning', FINISHED: 'danger' }[s] || ''
}

async function load(page) {
  query.pageNum = page || 1
  query.beginTime = timeRange.value?.[0] || undefined
  query.endTime = timeRange.value?.[1] || undefined
  loading.value = true
  try {
    const data = await listActivities(query)
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.type = ''
  query.status = ''
  query.keyword = ''
  timeRange.value = []
  load(1)
}

async function publish(row) {
  try {
    await ElMessageBox.confirm(
      row.published ? '重新生成宣传页？报名地址与已报名记录保持不变。' : '确认发布该活动？',
      '提示', { type: 'info' }
    )
    await publishActivity(row.id)
    ElMessage.success(row.published ? '宣传页已重新生成' : '活动已发布')
    load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      `删除后「${row.title}」的公开入口立即失效，历史数据保留。确认删除？`,
      '警告', { type: 'warning' }
    )
    await deleteActivity(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

onMounted(() => load())
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
