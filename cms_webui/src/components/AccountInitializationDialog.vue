<template>
  <el-dialog
    :model-value="visible"
    width="760px"
    align-center
    class="initialization-dialog"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="false"
  >
    <div class="init-panel">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step v-for="step in steps" :key="step" :title="step" />
      </el-steps>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="init-form">
        <section v-show="activeStep === 0" class="welcome-step">
          <h2>欢迎使用物业管理系统</h2>
          <p>首次登录需要完成账户初始化。接下来会引导您确认账户、完善生日和联系方式等基本信息，完成后即可进入系统。</p>
        </section>

        <section v-show="activeStep === 1">
          <h2>基本信息</h2>
          <div class="avatar-editor-wrapper">
            <AvatarEditor v-model="form.userAvata" :initial="avatarInitial" :size="96" />
          </div>
          <el-form-item label="ID">
            <el-input :model-value="form.id" disabled />
          </el-form-item>
          <el-form-item label="账户" prop="uid">
            <el-input v-model="form.uid" />
            <p class="form-tip">账户确认后将被锁定，之后不能再修改。</p>
          </el-form-item>
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" />
          </el-form-item>
          <el-form-item label="用户角色">
            <el-input :model-value="form.roleName" disabled />
          </el-form-item>
          <el-form-item label="性别" prop="userGender">
            <el-radio-group v-model="form.userGender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
              <el-radio :value="null">不愿透露</el-radio>
            </el-radio-group>
          </el-form-item>
        </section>

        <section v-show="activeStep === 2">
          <h2>设置生日</h2>
          <el-form-item label="生日" prop="userBirthday">
            <el-date-picker v-model="form.userBirthday" type="date" value-format="YYYY-MM-DD" placeholder="请选择生日" style="width: 100%" />
          </el-form-item>
        </section>

        <section v-show="activeStep === 3">
          <h2>设置联系方式</h2>
          <el-form-item label="电话" prop="userPhone">
            <el-input v-model="form.userPhone" maxlength="11" placeholder="请输入手机号" clearable @input="handlePhoneInput" />
          </el-form-item>
          <el-form-item label="邮箱" prop="userEmail">
            <el-input v-model="form.userEmail" maxlength="64" placeholder="请输入邮箱地址" clearable />
          </el-form-item>
          <p class="form-tip contact-tip">本页可跳过，后续也可以在个人设置中补充。</p>
        </section>

        <section v-show="activeStep === 4" class="welcome-step">
          <h2>初始化完成</h2>
          <p>你的账户资料已经准备好。点击完成后保存信息并进入系统首页。</p>
        </section>
      </el-form>

      <div class="init-footer">
        <el-button :disabled="activeStep === 0 || submitting" @click="activeStep -= 1">上一步</el-button>
        <el-button v-if="activeStep === 3" :disabled="submitting" @click="activeStep += 1">跳过</el-button>
        <el-button v-if="activeStep < 4" type="primary" :loading="submitting" @click="nextStep">下一步</el-button>
        <el-button v-else type="primary" :loading="submitting" @click="submitInitialization">完成</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { initializeUser, getUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { usePageCacheStore } from '@/stores/pageCache'
import { genderRequiredRule, optionalEmailRule, optionalPhoneRule, sanitizePhone } from '@/utils/validators'
import AvatarEditor from '@/components/AvatarEditor.vue'

const router = useRouter()
const userStore = useUserStore()
const pageCacheStore = usePageCacheStore()
const formRef = ref(null)
const activeStep = ref(0)
const submitting = ref(false)
const steps = ['欢迎', '基本信息', '设置生日', '联系方式', '完成']

const visible = computed(() => Number(userStore.userInfo?.userInitialized) === 0)
const avatarInitial = computed(() => (form.userName || form.uid || 'U').charAt(0).toUpperCase())

const form = reactive({
  id: '',
  uid: '',
  userName: '',
  roleName: '',
  userBirthday: '',
  userGender: null,
  userPhone: '',
  userEmail: '',
  userAvata: ''
})

const rules = {
  uid: [{ required: true, message: '请输入账户', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userGender: [genderRequiredRule()],
  userBirthday: [{ required: true, message: '请选择生日', trigger: 'change' }],
  userPhone: [optionalPhoneRule()],
  userEmail: [optionalEmailRule()]
}

function handlePhoneInput(value) {
  form.userPhone = sanitizePhone(value)
}

watch(
  () => userStore.userInfo,
  user => {
    if (!user) return
    Object.assign(form, {
      id: user.id || '',
      uid: user.uid || '',
      userName: user.userName || '',
      roleName: user.roleName || '',
      userBirthday: user.userBirthday || '',
      userGender: user.userGender ?? null,
      userPhone: user.userPhone || '',
      userEmail: user.userEmail || '',
      userAvata: user.userAvata || ''
    })
  },
  { immediate: true }
)

async function nextStep() {
  if (activeStep.value === 1) {
    await formRef.value.validateField(['uid', 'userName', 'userGender'])
  }
  if (activeStep.value === 2) {
    await formRef.value.validateField(['userBirthday'])
  }
  if (activeStep.value === 3) {
    await formRef.value.validateField(['userPhone', 'userEmail'])
  }
  activeStep.value += 1
}

async function submitInitialization() {
  submitting.value = true
  try {
    await initializeUser({
      uid: form.uid,
      userName: form.userName,
      userBirthday: form.userBirthday,
      userGender: form.userGender,
      userPhone: form.userPhone,
      userEmail: form.userEmail,
      userAvata: form.userAvata
    })
    const res = await getUserInfo({ forceRefresh: true })
    userStore.updateUserInfo(res.data)
    pageCacheStore.clearAll()
    ElMessage.success('账户初始化完成')
    router.push('/welcome')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.init-panel { min-height: 560px; display: flex; flex-direction: column; }
.init-form { flex: 1; padding: 30px 8px 12px; }
.init-form h2 { margin: 0 0 22px; font-size: 24px; color: var(--vp-c-text-1); text-align: center; }
.welcome-step { max-width: 520px; margin: 90px auto 0; text-align: center; }
.welcome-step p,
.form-tip { margin: 8px 0 0; color: var(--vp-c-text-2); line-height: 1.7; }
.avatar-editor-wrapper { display: flex; justify-content: center; margin-bottom: 22px; }
.contact-tip { padding-left: 100px; }
.init-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 18px; border-top: 1px solid var(--vp-c-divider); }

:global(.initialization-dialog .el-dialog) {
  border-radius: 16px;
}

:global(.initialization-dialog .el-dialog__header) {
  display: none;
}

@media (max-width: 768px) {
  :global(.initialization-dialog.el-dialog) {
    width: calc(100% - 24px) !important;
  }

  .init-panel { min-height: 600px; }
  .init-form { padding: 24px 0 12px; }
  .contact-tip { padding-left: 0; }
}
</style>
