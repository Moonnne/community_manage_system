<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="auth-logo">
          <el-icon :size="32" color="var(--vp-c-brand-1)"><OfficeBuilding /></el-icon>
        </div>
        <h1 class="auth-title">注册账户</h1>
        <p class="auth-subtitle">创建您的社区物业管理系统账户</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form" @keyup.enter="handleRegister">
        <el-form-item prop="uid">
          <el-input v-model="form.uid" placeholder="请输入账户" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" :prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item prop="userPassword">
          <el-input v-model="form.userPassword" type="password" placeholder="请输入密码 (8-20位包含数字和字母)" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input-otp v-model="form.captchaCode" :length="4" inputmode="text" :validator="captchaInputValidator" />
            <img class="captcha-img" :src="captchaUrl" alt="验证码" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <span class="auth-footer-text">已有账户?</span>
        <router-link to="/login" class="auth-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { captchaInputValidator, captchaRule } from '@/utils/validators'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  uid: '',
  userName: '',
  userPassword: '',
  confirmPassword: '',
  captchaCode: ''
})

const captchaUrl = ref('')

const validatePassword = (rule, value, callback) => {
  const reg = /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!reg.test(value)) {
    callback(new Error('密码必须为8-20位，且必须包含数字和字母'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.value.userPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  uid: [{ required: true, message: '请输入账户', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  captchaCode: [captchaRule()]
}

const refreshCaptcha = () => {
  captchaUrl.value = `/api/user/captcha?t=${Date.now()}`
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    await request.post('/api/user/register', {
      uid: form.value.uid,
      userName: form.value.userName,
      userPassword: form.value.userPassword,
      captchaCode: form.value.captchaCode
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    refreshCaptcha()
    form.value.captchaCode = ''
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
  linear-gradient(rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0.4)),
  url('https://oss.moonne.top/web/background.png') center / cover no-repeat;
  box-sizing: border-box;
}

.auth-container {
  width: 400px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.88);
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
  box-shadow: var(--vp-card-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  margin-bottom: 16px;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--vp-c-text-1);
  margin: 0 0 8px;
}

.auth-subtitle {
  font-size: 13px;
  color: var(--vp-c-text-3);
  margin: 0;
}

.auth-form {
  margin-top: 24px;
}

.auth-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
}

.captcha-row {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  cursor: pointer;
  background-color: #fff;
  object-fit: cover;
}

.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
}

.auth-footer-text {
  color: var(--vp-c-text-3);
  margin-right: 4px;
}

.auth-link {
  color: var(--vp-c-brand-1);
  font-weight: 500;
}

.auth-link:hover {
  color: var(--vp-c-brand-2);
}
</style>
