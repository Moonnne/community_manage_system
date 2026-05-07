<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">个人设置</h2></div>

    <el-row :gutter="24">
      <el-col :span="14">
        <el-card shadow="never" class="settings-card">
          <template #header>
            <span class="card-title">基本信息</span>
          </template>
          <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px">
            <el-form-item label="账户">
              <el-input :model-value="userStore.userInfo?.uid" disabled />
            </el-form-item>
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="infoForm.userName" />
            </el-form-item>
            <el-form-item label="电话">
              <el-input v-model="infoForm.userPhone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="infoForm.userEmail" />
            </el-form-item>
            <el-form-item label="生日">
              <el-date-picker v-model="infoForm.userBirthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="infoForm.userGender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="settings-card">
          <template #header>
            <span class="card-title">修改密码</span>
          </template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUser, changePassword } from '@/api/user'

const userStore = useUserStore()
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = ref({
  id: null,
  userName: '',
  userPhone: '',
  userEmail: '',
  userBirthday: '',
  userGender: 1
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function loadUserInfo() {
  try {
    const res = await getUserInfo()
    const u = res.data
    infoForm.value = {
      id: u.id,
      userName: u.userName,
      userPhone: u.userPhone || '',
      userEmail: u.userEmail || '',
      userBirthday: u.userBirthday || '',
      userGender: u.userGender || 1
    }
    userStore.updateUserInfo(u)
  } catch (e) { /* handled by interceptor */ }
}

async function handleUpdateInfo() {
  await infoFormRef.value.validate()
  infoLoading.value = true
  try {
    await updateUser(infoForm.value)
    ElMessage.success('信息更新成功')
    loadUserInfo()
  } finally {
    infoLoading.value = false
  }
}

async function handleChangePassword() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } finally {
    pwdLoading.value = false
  }
}

onMounted(loadUserInfo)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.settings-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.card-title { font-weight: 600; color: var(--vp-c-text-1); }
</style>
