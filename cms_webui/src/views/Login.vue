<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="auth-logo">
          <el-icon :size="32" color="var(--vp-c-brand-1)"><OfficeBuilding /></el-icon>
        </div>
        <h1 class="auth-title">翻斗花园物业管理系统</h1>
        <p class="auth-subtitle">Community Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form" @keyup.enter="handleLogin">
        <el-form-item prop="uid">
          <el-input v-model="form.uid" placeholder="请输入账户" :prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="userPassword">
          <el-input v-model="form.userPassword" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <span class="auth-footer-text">还没有账户?</span>
        <router-link to="/register" class="auth-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  uid: '',
  userPassword: ''
})

const rules = {
  uid: [{ required: true, message: '请输入账户', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await request.post('/api/user/login', form.value)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/welcome')
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-container {
  width: 400px;
  padding: 40px;
  background-color: var(--vp-card-bg);
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
  letter-spacing: 1px;
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
