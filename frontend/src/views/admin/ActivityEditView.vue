<template>
  <el-card>
    <template #header>
      <span>{{ isEdit ? '编辑活动' : '新建活动' }}</span>
    </template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 760px">
      <el-form-item label="活动名称" prop="title">
        <el-input v-model="form.title" maxlength="100" placeholder="请输入活动名称" />
      </el-form-item>
      <el-form-item label="活动类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择类型" style="width: 220px">
          <el-option v-for="t in types" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="活动简介">
        <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="2000" placeholder="填写活动简介，用于宣传页展示" />
      </el-form-item>
      <el-form-item label="活动地点" prop="location">
        <el-input v-model="form.location" maxlength="100" placeholder="请输入活动地点" />
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="选择开始时间" />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="选择结束时间" />
      </el-form-item>
      <el-form-item label="报名截止时间" prop="signupDeadline">
        <el-date-picker v-model="form.signupDeadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                        placeholder="选择报名截止时间" />
      </el-form-item>
      <el-form-item label="人数上限" prop="capacity">
        <el-input-number v-model="form.capacity" :min="1" :max="10000" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createActivity, getActivity, updateActivity } from '../../api/modules'

const route = useRoute()
const router = useRouter()
const types = ['讲座', '宣讲会', '就业指导', '其他']
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  title: '', type: '', intro: '', location: '',
  startTime: '', endTime: '', signupDeadline: '', capacity: 100
})

const rules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    { validator: (rule, value, cb) =>
        value && form.startTime && new Date(value) <= new Date(form.startTime)
          ? cb(new Error('结束时间必须晚于开始时间')) : cb(), trigger: 'change' }
  ],
  signupDeadline: [
    { required: true, message: '请选择报名截止时间', trigger: 'change' },
    { validator: (rule, value, cb) =>
        value && form.startTime && new Date(value) > new Date(form.startTime)
          ? cb(new Error('报名截止时间不得晚于开始时间')) : cb(), trigger: 'change' }
  ],
  capacity: [{ required: true, message: '请输入人数上限', trigger: 'change' }]
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const data = await getActivity(route.params.id)
      Object.assign(form, {
        title: data.title, type: data.type, intro: data.intro, location: data.location,
        startTime: data.startTime, endTime: data.endTime,
        signupDeadline: data.signupDeadline, capacity: data.capacity
      })
    } catch (e) {
      ElMessage.error('活动信息加载失败，请返回列表重试')
    }
  }
})

async function onSubmit() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateActivity(route.params.id, form)
      ElMessage.success('活动已更新，宣传页展示最新信息')
      router.push(`/admin/activities/${route.params.id}`)
    } else {
      const id = await createActivity(form)
      ElMessage.success('活动创建成功')
      router.push(`/admin/activities/${id}`)
    }
  } finally {
    saving.value = false
  }
}
</script>
