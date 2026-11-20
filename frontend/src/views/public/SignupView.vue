<template>
  <div class="page-container">
    <div class="promo-card" v-if="loadError">
      <el-result icon="warning" title="活动信息加载失败" sub-title="请检查网络后刷新页面重试">
        <template #extra>
          <el-button @click="$router.go(0)">重新加载</el-button>
        </template>
      </el-result>
    </div>
    <div class="promo-card" v-else>
      <h2 style="margin-top: 0">活动报名</h2>
      <p class="promo-meta">{{ activity.title }} · 截止 {{ activity.signupDeadline }}</p>
      <p v-if="!success" style="margin: 0 0 16px; color: #909399; font-size: 13px">
        同一姓名或手机号在同一活动中只能报名一次。
      </p>

      <el-result v-if="success" icon="success" title="报名成功">
        <template #extra>
          <el-button type="primary" @click="$router.push(`/a/${id}`)">返回活动宣传页</el-button>
        </template>
      </el-result>

      <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名（2~20字符）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入大陆11位手机号" />
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" size="large" :loading="submitting"
                     @click="onSubmit">提交报名</el-button>
        </el-form-item>
      </el-form>

      <p v-if="loaded && !activity.signupAvailable" style="color: #f56c6c; text-align: center">
        当前活动不在报名时段内，报名已结束。
      </p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicActivity, getPublicColleges, signup } from '../../api/modules'

const id = useRoute().params.id
const activity = reactive({})
const colleges = ref([])
const success = ref(false)
const submitting = ref(false)
const loaded = ref(false)
const loadError = ref(false)
const formRef = ref()
const form = reactive({ name: '', phone: '', collegeId: null })

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名须为2~20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }]
}

onMounted(async () => {
  try {
    const [act, cs] = await Promise.all([getPublicActivity(id), getPublicColleges()])
    Object.assign(activity, act)
    colleges.value = cs
    loaded.value = true
  } catch (e) {
    loadError.value = true
  }
})

async function onSubmit() {
  if (!activity.signupAvailable) {
    ElMessage.warning('报名已结束')
    return
  }
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    await signup(id, form)
    success.value = true // 失败原因由 Axios 拦截器统一提示，不新增记录
  } finally {
    submitting.value = false
  }
}
</script>
