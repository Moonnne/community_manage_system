<template>
  <div class="welcome-page">
    <tech-background />
    <div class="welcome-content">
      <!-- Hero Section -->
      <div class="hero">
        <h1 class="hero-title">
          翻斗花园物业管理系统
        </h1>
        <p class="hero-tagline">高效、便捷、智能的社区物业一站式管理平台</p>
      </div>

      <!-- Feature Cards -->
      <div class="features">
        <div class="feature-card" v-for="f in visibleFeatures" :key="f.title" @click="$router.push(f.path)">
          <div class="feature-icon">
            <el-icon :size="28"><component :is="f.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import TechBackground from '@/components/TechBackground.vue'

const userStore = useUserStore()
const role = computed(() => userStore.userRole)

const allFeatures = [
  { title: '用户管理', desc: '管理系统用户、权限分配', icon: 'User', path: '/user/list', roles: [0, 1] },
  { title: '财务管理', desc: '账单管理、费用缴纳', icon: 'Wallet', path: '/finance/dashboard', roles: [0, 1, 2, 3, 4, 5] },
  { title: '社区安全', desc: '访客登记、车辆通行管理', icon: 'Lock', path: '/security/visitor', roles: [0, 1, 3] },
  { title: '公共设施', desc: '设施维护、巡检管理', icon: 'OfficeBuilding', path: '/facility/list', roles: [0, 1, 2] },
  { title: '资产管理', desc: '房屋资产、产权登记', icon: 'House', path: '/asset/property', roles: [0, 1, 2, 4] },
  { title: '工单管理', desc: '报修工单、任务分配', icon: 'Tickets', path: '/task/list', roles: [0, 1, 2, 3, 4] }
]

const visibleFeatures = computed(() => allFeatures.filter(f => f.roles.includes(role.value)))
</script>

<style scoped>
.welcome-page {
  position: relative;
  min-height: calc(100vh - var(--vp-nav-height) - 48px);
}

.welcome-content {
  position: relative;
  z-index: 1;
  max-width: 960px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 60px 0 48px;
}

.hero-title {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: -0.5px;
  line-height: 1.2;
  color: var(--vp-c-text-1);
  margin: 0 0 16px;
}

[data-theme='dark'] .hero-title {
  color: var(--vp-c-brand-1);
}

.hero-tagline {
  font-size: 18px;
  color: var(--vp-c-text-2);
  margin: 0 0 32px;
  font-weight: 400;
}

/* Features */
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 20px 0 40px;
}

.feature-card {
  padding: 24px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  background-color: var(--vp-card-bg);
  box-shadow: var(--vp-card-shadow);
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-card:hover {
  border-color: var(--vp-c-brand-1);
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

[data-theme='dark'] .feature-card:hover {
  transform: none;
  box-shadow: var(--vp-card-shadow);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  border-radius: 10px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
  background-color: var(--vp-c-brand-1);
  color: #ffffff;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--vp-c-text-1);
  margin: 0 0 8px;
}

.feature-desc {
  font-size: 13px;
  color: var(--vp-c-text-3);
  margin: 0;
  line-height: 1.6;
}
</style>
