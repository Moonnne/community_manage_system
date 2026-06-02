<template>
  <div class="layout">
    <!-- NavBar -->
    <header class="nav-bar">
      <div class="nav-bar-inner">
        <div class="nav-bar-left">
          <button type="button" class="mobile-menu-button" @click="mobileMenuVisible = true">
            <el-icon :size="22"><Menu /></el-icon>
          </button>
          <router-link to="/welcome" class="nav-logo desktop-logo">
            <el-icon :size="24" color="var(--vp-c-brand-1)"><OfficeBuilding /></el-icon>
            <span class="nav-logo-text">翻斗花园物业管理系统</span>
          </router-link>
        </div>
        <nav class="nav-bar-center desktop-nav">
          <router-link
            v-for="item in visibleNavItems"
            :key="item.path"
            :to="item.path"
            :class="['nav-item', { 'nav-item-active': isNavActive(item.path) }]"
          >
            <el-icon :size="16"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>
        <div class="nav-bar-right">
          <!-- Theme Toggle -->
          <div class="theme-toggle" @click="toggleTheme">
            <el-icon :size="20">
              <Moon v-if="theme === 'light'" />
              <Sunny v-else />
            </el-icon>
          </div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="nav-avatar">
              <el-avatar :size="32" :src="userStore.userInfo?.userAvata" :style="{ backgroundColor: 'var(--vp-c-brand-1)' }">
                {{ avatarText }}
              </el-avatar>
              <span class="nav-username">{{ userStore.userInfo?.userName || '用户' }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>个人设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <el-drawer v-model="mobileMenuVisible" direction="ltr" size="280px" class="mobile-nav-drawer" :with-header="false">
      <div class="mobile-drawer-header">
        <router-link to="/welcome" class="mobile-drawer-logo" @click="closeMobileMenu">
          <el-icon :size="26" color="var(--vp-c-brand-1)"><OfficeBuilding /></el-icon>
          <span>翻斗花园物业管理系统</span>
        </router-link>
      </div>

      <div class="mobile-drawer-section">
        <router-link
          to="/welcome"
          :class="['mobile-drawer-item', { 'mobile-drawer-item-active': $route.path === '/welcome' }]"
          @click="closeMobileMenu"
        >
          <el-icon :size="18"><House /></el-icon>
          <span>首页</span>
        </router-link>

        <div v-for="item in visibleNavItems" :key="item.path" class="mobile-drawer-nav-block">
          <router-link
            :to="item.path"
            :class="['mobile-drawer-item', { 'mobile-drawer-item-active': isNavActive(item.path) }]"
            @click="closeMobileMenu"
          >
            <el-icon :size="18"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>

          <div v-if="isNavActive(item.path) && sidebarItems.length > 0" class="mobile-drawer-subnav">
            <div class="mobile-drawer-group" v-for="group in sidebarItems" :key="group.title">
              <router-link
                v-for="child in group.children"
                :key="child.path"
                :to="child.path"
                :class="['mobile-drawer-subitem', { 'mobile-drawer-subitem-active': $route.path === child.path }]"
                @click="closeMobileMenu"
              >
                {{ child.label }}
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="mobile-drawer-footer">
        <router-link
          to="/settings"
          :class="['mobile-drawer-item', { 'mobile-drawer-item-active': $route.path === '/settings' }]"
          @click="closeMobileMenu"
        >
          <el-icon :size="18"><Setting /></el-icon>
          <span>个人设置</span>
        </router-link>
      </div>
    </el-drawer>

    <!-- Content -->
    <div class="layout-content">
      <!-- SideBar -->
      <aside v-if="sidebarItems.length > 0" class="sidebar">
        <div class="sidebar-inner">
          <div class="sidebar-group" v-for="group in sidebarItems" :key="group.title">
            <p class="sidebar-group-title">{{ group.title }}</p>
            <router-link
              v-for="item in group.children"
              :key="item.path"
              :to="item.path"
              :class="['sidebar-item', { 'sidebar-item-active': $route.path === item.path }]"
            >
              {{ item.label }}
            </router-link>
          </div>
        </div>
      </aside>

      <!-- Main -->
      <main :class="['main-content', { 'no-sidebar': sidebarItems.length === 0 }]">
        <router-view />
      </main>
    </div>

    <AccountInitializationDialog />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import AccountInitializationDialog from '@/components/AccountInitializationDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const theme = ref(localStorage.getItem('cms_theme') || 'light')
const mobileMenuVisible = ref(false)

const toggleTheme = (event) => {
  const x = event.clientX
  const y = event.clientY
  const endRadius = Math.hypot(
    Math.max(x, innerWidth - x),
    Math.max(y, innerHeight - y)
  )

  // @ts-ignore
  if (!document.startViewTransition) {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    applyTheme()
    return
  }

  // @ts-ignore
  const transition = document.startViewTransition(() => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    applyTheme()
  })

  transition.ready.then(() => {
    const clipPath = [
      `circle(0px at ${x}px ${y}px)`,
      `circle(${endRadius}px at ${x}px ${y}px)`
    ]
    document.documentElement.animate(
      {
        clipPath: clipPath
      },
      {
        duration: 500,
        easing: 'ease-in-out',
        pseudoElement: '::view-transition-new(root)'
      }
    )
  })
}

const applyTheme = () => {
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem('cms_theme', theme.value)
}

const avatarText = computed(() => {
  const name = userStore.userInfo?.userName || ''
  return name.charAt(0).toUpperCase() || 'U'
})

const allNavItems = [
  { path: '/user', label: '用户管理', icon: 'User', roles: [0, 1] },
  { path: '/finance', label: '财务管理', icon: 'Wallet', roles: [0, 1, 2, 3, 4, 5] },
  { path: '/security', label: '社区安全', icon: 'Lock', roles: [0, 1, 3] },
  { path: '/facility', label: '公共设施', icon: 'OfficeBuilding', roles: [0, 1, 2] },
  { path: '/asset', label: '资产管理', icon: 'House', roles: [0, 1, 2, 4] },
  { path: '/task', label: '工单管理', icon: 'Tickets', roles: [0, 1, 2, 3, 4] }
]

const visibleNavItems = computed(() => {
  const role = userStore.userRole
  return allNavItems.filter(item => item.roles.includes(role))
})

function isNavActive(path) {
  return route.path.startsWith(path)
}

const sidebarConfig = {
  '/user': [
    {
      title: '信息管理',
      children: [{ path: '/user/list', label: '用户信息' }],
      roles: [0, 1]
    },
    {
      title: '类型管理',
      children: [{ path: '/user/role', label: '权限管理' }],
      roles: [0]
    }
  ],
  '/finance': [
    {
      title: '信息管理',
      children: [{ path: '/finance/dashboard', label: '财务面板' }],
      roles: [0, 1, 2, 3, 4, 5]
    },
    {
      title: '账单管理',
      children: [{ path: '/finance/bill', label: '账单管理' }],
      roles: [0, 1, 2]
    },
    {
      title: '类型管理',
      children: [{ path: '/finance/type', label: '账单类型' }],
      roles: [0, 1]
    }
  ],
  '/security': [
    {
      title: '信息管理',
      children: [
        { path: '/security/dashboard', label: '社区安全面板' },
        { path: '/security/visitor', label: '访客管理' },
        { path: '/security/car', label: '车辆管理' },
        { path: '/security/car-history', label: '通行记录' }
      ],
      roles: [0, 1, 3]
    },
    {
      title: '类型管理',
      children: [
        { path: '/security/visitor-type', label: '访客类型' },
        { path: '/security/gate', label: '门岗管理' }
      ],
      roles: [0, 1]
    }
  ],
  '/facility': [
    {
      title: '信息管理',
      children: [
        { path: '/facility/dashboard', label: '公共设施面板' },
        { path: '/facility/list', label: '设施信息' }
      ],
      roles: [0, 1, 2]
    },
    {
      title: '类型管理',
      children: [{ path: '/facility/type', label: '设施类型' }],
      roles: [0, 1]
    }
  ],
  '/asset': [
    {
      title: '信息管理',
      children: [
        { path: '/asset/dashboard', label: '资产面板' },
        { path: '/asset/property', label: '房产管理' },
        { path: '/asset/parking', label: '停车位管理' }
      ],
      roles: [0, 1, 2, 4]
    }
  ],
  '/task': [
    {
      title: '信息管理',
      children: [
        { path: '/task/dashboard', label: '工单面板' },
        { path: '/task/list', label: '工单列表' }
      ],
      roles: [0, 1, 2, 3, 4]
    },
    {
      title: '类型管理',
      children: [{ path: '/task/type', label: '工单类型' }],
      roles: [0, 1]
    }
  ]
}

const sidebarItems = computed(() => {
  const role = userStore.userRole
  const prefix = '/' + route.path.split('/')[1]
  const config = sidebarConfig[prefix]
  if (!config) return []
  return config
    .filter(group => group.roles.includes(role))
    .map(group => {
      if (role >= 4 && (prefix === '/asset' || prefix === '/task')) {
        return {
          ...group,
          children: group.children.filter(item => item.path.endsWith('/dashboard'))
        }
      }
      return group
    })
    .filter(group => group.children.length > 0)
})

function handleCommand(command) {
  if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}

function closeMobileMenu() {
  mobileMenuVisible.value = false
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

/* NavBar */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--vp-nav-height);
  background-color: var(--vp-c-bg-soft);
  border-bottom: 1px solid var(--vp-c-divider);
  padding: 0 24px;
  white-space: nowrap;
}

.nav-bar-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-bar-left {
  flex-shrink: 0;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.nav-logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

.mobile-menu-button {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--vp-c-text-1);
}

.nav-bar-center {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 40px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.nav-bar-center::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  height: 40px;
  border-radius: 8px;
  color: var(--vp-c-text-2);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item:hover {
  color: var(--vp-c-brand-1);
  background-color: var(--vp-c-brand-soft);
}

.nav-item-active {
  color: var(--vp-c-brand-1);
  background-color: var(--vp-c-brand-soft);
}

[data-theme='dark'] .nav-item-active {
  background-color: var(--vp-c-brand-soft);
}

.nav-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.theme-toggle {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--vp-c-text-1);
  transition: background-color 0.3s;
}

.theme-toggle:hover {
  background-color: var(--vp-c-brand-soft);
}

.theme-toggle:hover {
  background-color: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
}

.nav-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.25s;
}

.nav-avatar:hover {
  background-color: var(--vp-c-bg-soft);
}

.nav-username {
  font-size: 13px;
  font-weight: 500;
  color: var(--vp-c-text-2);
}

/* Sidebar */
.layout-content {
  padding-top: var(--vp-nav-height);
  display: flex;
  min-height: calc(100vh - var(--vp-nav-height));
}

.sidebar {
  position: fixed;
  top: var(--vp-nav-height);
  left: 0;
  bottom: 0;
  width: var(--vp-sidebar-width);
  border-right: 1px solid var(--vp-c-divider);
  background-color: var(--vp-c-bg-soft);
  overflow-y: auto;
  z-index: 90;
}

.sidebar-inner {
  padding: 24px 20px;
}

.sidebar-group {
  margin-bottom: 20px;
}

.sidebar-group-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--vp-c-text-3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0 8px;
  margin-bottom: 8px;
}

.sidebar-item {
  display: block;
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--vp-c-text-2);
  text-decoration: none;
  border-radius: 6px;
  margin-bottom: 2px;
  border-left: 2px solid transparent;
  transition: color 0.25s, background-color 0.25s, border-color 0.25s;
}

.sidebar-item:hover {
  color: var(--vp-c-brand-1);
  background-color: var(--vp-c-brand-soft);
}

.sidebar-item-active {
  color: var(--vp-c-brand-1);
  background-color: var(--vp-c-brand-soft);
  border-left-color: var(--vp-c-brand-1);
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: var(--vp-sidebar-width);
  padding: 24px 32px;
  max-width: calc(100% - var(--vp-sidebar-width));
}

.main-content.no-sidebar {
  margin-left: 0;
  max-width: 100%;
}

.mobile-drawer-header {
  padding: 6px 0 18px;
  border-bottom: 1px solid var(--vp-c-divider);
  margin-bottom: 14px;
}

:deep(.mobile-nav-drawer .el-drawer__body) {
  display: flex;
  flex-direction: column;
}

.mobile-drawer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--vp-c-text-1);
  font-size: 15px;
  font-weight: 700;
}

.mobile-drawer-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  margin-bottom: 18px;
}

.mobile-drawer-nav-block {
  display: flex;
  flex-direction: column;
}

.mobile-drawer-item,
.mobile-drawer-subitem {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 12px;
  border-radius: 8px;
  color: var(--vp-c-text-2);
  font-size: 14px;
  font-weight: 500;
}

.mobile-drawer-item-active,
.mobile-drawer-subitem-active {
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.mobile-drawer-subnav {
  margin: 8px 0 12px 31px;
  padding-left: 18px;
  border-left: 2px solid var(--vp-c-divider);
}

.mobile-drawer-group {
  margin-bottom: 6px;
}

.mobile-drawer-subitem {
  min-height: 36px;
  padding-left: 14px;
  font-size: 13px;
}

.mobile-drawer-footer {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--vp-c-divider);
}

@media (max-width: 1180px) {
  .nav-bar {
    padding: 0 16px;
  }

  .nav-bar-center {
    margin: 0 20px;
  }

  .nav-item {
    padding: 0 12px;
  }

  .main-content {
    padding: 20px 24px;
  }
}

@media (max-width: 900px) {
  .nav-username {
    display: none;
  }

  .nav-bar-center {
    margin: 0 12px;
  }

  .sidebar {
    width: 200px;
  }

  .main-content {
    margin-left: 200px;
    max-width: calc(100% - 200px);
  }
}

@media (max-width: 768px) {
  .nav-bar {
    height: var(--vp-nav-height-mobile);
    padding: 8px 12px;
  }

  .nav-bar-inner {
    flex-wrap: nowrap;
    gap: 8px;
  }

  .nav-bar-left,
  .nav-bar-right {
    height: 40px;
  }

  .nav-bar-right {
    margin-left: auto;
    gap: 8px;
  }

  .desktop-logo,
  .desktop-nav {
    display: none;
  }

  .mobile-menu-button {
    display: flex;
  }

  .nav-avatar {
    display: flex !important;
    padding: 0;
    background: transparent;
  }

  .nav-avatar .el-icon {
    display: none;
  }

  .layout-content {
    padding-top: var(--vp-nav-height-mobile);
    display: block;
  }

  .sidebar {
    display: none;
  }

  .main-content {
    margin-left: 0;
    max-width: 100%;
    padding: 20px 14px;
  }

  .main-content.no-sidebar {
    padding-top: 20px;
  }
}

@media (max-width: 480px) {
  .nav-item span {
    display: none;
  }

  .nav-item {
    width: 38px;
    justify-content: center;
  }

  .theme-toggle,
  .nav-avatar {
    width: 36px;
    height: 36px;
  }

  .nav-avatar {
    justify-content: center;
  }
}
</style>
