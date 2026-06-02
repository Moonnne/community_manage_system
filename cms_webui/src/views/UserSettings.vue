<template>
  <div class="page-container settings-page">
    <div class="page-header">
      <h2 class="page-title">个人设置</h2>
    </div>

    <div class="settings-layout">
      <el-card shadow="never" class="settings-card profile-card">
        <div class="profile-head">
          <el-avatar :size="88" shape="circle" :src="infoForm.userAvata" :style="{ backgroundColor: 'var(--vp-c-brand-1)' }">
            {{ avatarInitial }}
          </el-avatar>
          <strong>{{ infoForm.userName || userInfo.userName || '-' }}</strong>
          <div class="profile-card-tags">
            <el-tag effect="plain" type="success">{{ valueText(userInfo.roleName) }}</el-tag>
            <el-tag effect="plain" :type="statusTagType(userInfo.userStatus)">{{ statusText(userInfo.userStatus) }}</el-tag>
          </div>
        </div>

        <el-descriptions :column="2" class="profile-details">
          <el-descriptions-item label="用户ID: ">{{ valueText(userInfo.id) }}</el-descriptions-item>
          <el-descriptions-item label="账户: ">{{ userInfo.uid || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名: ">{{ valueText(infoForm.userName || userInfo.userName) }}</el-descriptions-item>
          <el-descriptions-item label="性别: ">{{ genderText(infoForm.userGender) }}</el-descriptions-item>
        </el-descriptions>
        <el-descriptions :column="1" class="profile-details">
          <el-descriptions-item label="生日: ">{{ valueText(infoForm.userBirthday) }}</el-descriptions-item>
          <el-descriptions-item label="注册时间: ">{{ valueText(userInfo.userCreateTime) }}</el-descriptions-item>
        </el-descriptions>
        <el-descriptions :column="1" class="profile-details">
          <el-descriptions-item label="电话: ">{{ valueText(infoForm.userPhone) }}</el-descriptions-item>
          <el-descriptions-item label="邮箱: ">{{ valueText(infoForm.userEmail) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="settings-card settings-panels">
        <el-collapse>
          <el-collapse-item name="profile">
            <template #title>
              <span class="card-title">修改信息</span>
            </template>
            <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="96px" class="settings-form">
              <section class="settings-section">
                <div class="avatar-edit-panel">
                  <AvatarEditor v-model="infoForm.userAvata" :initial="avatarInitial" :size="96" @uploaded="handleAvatarUploaded" />
                </div>
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
                    <el-form-item label="电话" prop="userPhone">
                      <el-input v-model="infoForm.userPhone" maxlength="11" placeholder="请输入手机号" clearable @input="handleInfoPhoneInput" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :md="12">
                    <el-form-item label="邮箱" prop="userEmail">
                      <el-input v-model="infoForm.userEmail" maxlength="64" placeholder="请输入邮箱地址" clearable />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="性别" prop="userGender">
                      <el-radio-group v-model="infoForm.userGender">
                        <el-radio :value="0">男</el-radio>
                        <el-radio :value="1">女</el-radio>
                        <el-radio :value="2">不愿透露</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="form-actions">
                  <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
                </div>
              </section>
            </el-form>
          </el-collapse-item>

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
                  <el-input-otp v-model="pwdForm.captchaCode" :length="4" inputmode="text" :validator="captchaInputValidator" />
                  <img class="captcha-img" :src="captchaUrl" alt="验证码" @click="refreshCaptcha" />
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">确认修改</el-button>
              </el-form-item>
              <el-form-item>
                <el-link type="primary" :underline="false" @click="goForgotPassword">忘记原密码？提交工单以请求人工介入</el-link>
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUser, changePassword } from '@/api/user'
import { captchaInputValidator, captchaRule, genderRequiredRule, optionalEmailRule, optionalPhoneRule, sanitizePhone } from '@/utils/validators'
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
  userGender: undefined,
  userAvata: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  captchaCode: ''
})

const infoRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPhone: [optionalPhoneRule()],
  userEmail: [optionalEmailRule()],
  userGender: [genderRequiredRule()]
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
  captchaCode: [captchaRule()]
}

function handleInfoPhoneInput(value) {
  infoForm.value.userPhone = sanitizePhone(value)
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
    userGender: u.userGender ?? undefined,
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
  } catch (e) {
    pwdForm.value.captchaCode = ''
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

function statusTagType(status) {
  return ['success', 'warning', 'danger'][status] || 'info'
}

function genderText(gender) {
  return gender === 0 ? '男' : gender === 1 ? '女' : gender === 2 ? '不愿透露' : '-'
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
.settings-layout {
  display: grid;
  grid-template-columns: minmax(300px, 3fr) minmax(0, 7fr);
  align-items: start;
  gap: 16px;
}
.settings-form { max-width: 100%; }
.settings-section { padding: 4px 0; }
.settings-section h3 { margin: 0 0 18px; font-size: 16px; color: var(--vp-c-text-1); }
.profile-card {
  position: sticky;
  top: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--vp-c-brand-soft), transparent 56%), var(--vp-c-bg-soft);
}
.profile-card :deep(.el-card__body) { padding: 28px; }
.profile-head {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
}
.profile-head strong {
  display: block;
  margin-top: 4px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--vp-c-text-1);
  word-break: break-all;
}
.profile-head p { margin: 0; color: var(--vp-c-text-2); }
.profile-card-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 4px;
  color: var(--vp-c-text-2);
}
.profile-details { margin-top: 24px; }
.profile-details :deep(.el-descriptions__body) { background: transparent; }
.profile-details :deep(.el-descriptions__label) {
  min-width: 72px;
  color: var(--vp-c-text-2);
  font-weight: 500;
}
.profile-details :deep(.el-descriptions__content) {
  color: var(--vp-c-text-1);
  word-break: break-all;
}
.settings-panels :deep(.el-card__body) { padding-top: 8px; }
.avatar-edit-panel {
  display: flex;
  justify-content: center;
  margin-bottom: 22px;
}
.form-actions { padding-left: 96px; }
.card-title { font-weight: 700; color: var(--vp-c-text-1); }
.password-form { max-width: 760px; padding-top: 16px; }
.captcha-row { display: flex; align-items: center; gap: 12px; width: 100%; }
.captcha-img { width: 120px; height: 40px; border: 1px solid var(--vp-c-divider); border-radius: 6px; cursor: pointer; object-fit: cover; flex-shrink: 0; }

@media (max-width: 700px) {
  .settings-page {
    display: block;
    min-height: auto;
    overflow: visible;
    padding-bottom: 24px;
  }
  .settings-layout {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    min-height: auto;
    overflow: visible;
  }
  .profile-card {
    position: static;
    overflow: visible;
    width: 100%;
  }
  .settings-panels {
    overflow: visible;
    width: 100%;
  }
  .profile-card :deep(.el-card__body) { padding: 22px; }
  .profile-head strong { font-size: 24px; }
  .profile-details {
    --el-descriptions-table-border: none;
  }
  .profile-details :deep(.el-descriptions__table) {
    display: block;
  }
  .profile-details :deep(.el-descriptions__tbody),
  .profile-details :deep(.el-descriptions__tr),
  .profile-details :deep(.el-descriptions__cell) {
    display: block;
    width: 100% !important;
  }
  .profile-details :deep(.el-descriptions__cell) {
    padding: 0 0 12px;
  }
  .profile-details :deep(.el-descriptions__label),
  .profile-details :deep(.el-descriptions__content) {
    display: inline;
  }
  .form-actions { padding-left: 0; }
  .captcha-row { flex-direction: column; }
}
</style>
