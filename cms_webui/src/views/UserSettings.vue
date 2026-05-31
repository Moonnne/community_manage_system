<template>
  <div class="page-container settings-page">
    <div class="page-header">
      <h2 class="page-title">个人设置</h2>
    </div>

    <el-card shadow="never" class="settings-card">
      <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="96px" class="settings-form">
        <section class="settings-section profile-section">
          <AvatarEditor v-model="infoForm.userAvata" :initial="avatarInitial" :size="96" @uploaded="handleAvatarUploaded" />
          <div class="account-summary">
            <strong>{{ infoForm.userName || userInfo.userName || '-' }}</strong>
            <p>账号: {{ userInfo.uid || '-' }}</p>
            <p>ID: {{ valueText(userInfo.id) }}</p>
          </div>
        </section>

        <el-divider />

        <section class="settings-section">
          <h3>身份信息</h3>
          <el-row :gutter="18">
            <el-col :xs="24" :md="12">
              <el-form-item label="角色组">
                <el-input :model-value="valueText(userInfo.roleName)" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="账户状态">
                <el-input :model-value="statusText(userInfo.userStatus)" disabled />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="注册日期">
                <el-input :model-value="valueText(userInfo.userCreateTime)" disabled />
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <el-divider />

        <section class="settings-section">
          <h3>基础资料</h3>
          <el-row :gutter="18">
            <el-col :xs="24" :md="12">
              <el-form-item label="用户名" prop="userName">
                <el-input v-model="infoForm.userName" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="生日">
                <el-date-picker v-model="infoForm.userBirthday" type="date" value-format="YYYY-MM-DD" placeholder="选择生日" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="电话">
                <el-input v-model="infoForm.userPhone" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="邮箱">
                <el-input v-model="infoForm.userEmail" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="性别">
                <el-radio-group v-model="infoForm.userGender">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                  <el-radio :value="null">不愿透露</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <div class="form-actions">
            <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
          </div>
        </section>
      </el-form>
    </el-card>

    <el-card shadow="never" class="settings-card password-card">
      <el-collapse>
        <el-collapse-item name="password">
          <template #title>
            <span class="card-title">修改密码</span>
          </template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="password-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="验证码" prop="captchaCode">
              <div class="captcha-row">
                <el-input v-model="pwdForm.captchaCode" maxlength="4" />
                <img class="captcha-img" :src="captchaUrl" alt="验证码" @click="refreshCaptcha" />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">确认修改</el-button>
              <el-link type="primary" :underline="false" @click="goForgotPassword">忘记原密码？提交工单</el-link>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUser, changePassword } from '@/api/user'
import AvatarEditor from '@/components/AvatarEditor.vue'

const router = useRouter()
const userStore = useUserStore()
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)
const captchaUrl = ref('')

const userInfo = computed(() => userStore.userInfo || {})
const avatarInitial = computed(() => (infoForm.value.userName || userInfo.value.uid || 'U').charAt(0).toUpperCase())

const infoForm = ref({
  id: null,
  userName: '',
  userPhone: '',
  userEmail: '',
  userBirthday: '',
  userGender: null,
  userAvata: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  captchaCode: ''
})

const infoRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadUserInfo() {
  const res = await getUserInfo({ forceRefresh: true })
  const u = res.data
  infoForm.value = {
    id: u.id,
    userName: u.userName || '',
    userPhone: u.userPhone || '',
    userEmail: u.userEmail || '',
    userBirthday: u.userBirthday || '',
    userGender: u.userGender ?? null,
    userAvata: u.userAvata || ''
  }
  userStore.updateUserInfo(u)
}

async function handleUpdateInfo() {
  await infoFormRef.value.validate()
  infoLoading.value = true
  try {
    await saveProfile()
    ElMessage.success('个人资料已更新')
    await loadUserInfo()
  } finally {
    infoLoading.value = false
  }
}

async function handleAvatarUploaded(url) {
  infoForm.value.userAvata = url
  userStore.updateUserInfo({ userAvata: url })
  await saveProfile()
}

async function saveProfile() {
  await updateUser({
    id: infoForm.value.id,
    userName: infoForm.value.userName,
    userBirthday: infoForm.value.userBirthday || null,
    userGender: infoForm.value.userGender,
    userPhone: infoForm.value.userPhone,
    userEmail: infoForm.value.userEmail,
    userAvata: infoForm.value.userAvata
  })
}

async function handleChangePassword() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
      captchaCode: pwdForm.value.captchaCode
    })
    ElMessage.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '', captchaCode: '' }
    refreshCaptcha()
  } finally {
    pwdLoading.value = false
  }
}

function refreshCaptcha() {
  captchaUrl.value = `/api/user/captcha?t=${Date.now()}`
}

function goForgotPassword() {
  router.push('/task/list?intent=forgot-password')
}

function valueText(value) {
  return value === '' || value === undefined || value === null ? '-' : value
}

function statusText(status) {
  return ['正常', '冻结', '禁用'][status] || '未知'
}

onMounted(() => {
  loadUserInfo()
  refreshCaptcha()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.settings-page { display: grid; gap: 16px; }
.settings-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.settings-form { max-width: 1180px; }
.settings-section { padding: 4px 0; }
.settings-section h3 { margin: 0 0 18px; font-size: 16px; color: var(--vp-c-text-1); }
.profile-section { display: flex; align-items: center; gap: 22px; }
.account-summary strong { display: block; font-size: 32px; line-height: 1.2; color: var(--vp-c-text-1); word-break: break-all; }
.account-summary p { margin: 8px 0 0; color: var(--vp-c-text-2); }
.form-actions { padding-left: 96px; }
.password-card :deep(.el-card__body) { padding-top: 8px; }
.card-title { font-weight: 700; color: var(--vp-c-text-1); }
.password-form { max-width: 760px; padding-top: 16px; }
.captcha-row { display: flex; gap: 12px; width: 100%; }
.captcha-img { width: 120px; height: 40px; border: 1px solid var(--vp-c-divider); border-radius: 6px; cursor: pointer; object-fit: cover; flex-shrink: 0; }

@media (max-width: 768px) {
  .profile-section { align-items: flex-start; gap: 18px; }
  .account-summary strong { font-size: 24px; }
  .form-actions { padding-left: 0; }
  .captcha-row { flex-direction: column; }
}
</style>
