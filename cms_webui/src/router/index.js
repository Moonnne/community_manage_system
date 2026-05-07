import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: 'welcome',
        name: 'Welcome',
        component: () => import('@/views/Welcome.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user',
        name: 'UserManage',
        redirect: '/user/list',
        children: [
          {
            path: 'list',
            name: 'UserList',
            component: () => import('@/views/user/UserList.vue'),
            meta: { title: '用户信息' }
          },
          {
            path: 'role',
            name: 'UserRole',
            component: () => import('@/views/user/UserRole.vue'),
            meta: { title: '权限管理' }
          }
        ]
      },
      {
        path: 'finance',
        name: 'FinanceManage',
        redirect: '/finance/bill',
        children: [
          {
            path: 'bill',
            name: 'BillList',
            component: () => import('@/views/finance/BillList.vue'),
            meta: { title: '账单管理' }
          },
          {
            path: 'type',
            name: 'BillType',
            component: () => import('@/views/finance/BillType.vue'),
            meta: { title: '账单类型' }
          }
        ]
      },
      {
        path: 'security',
        name: 'SecurityManage',
        redirect: '/security/visitor',
        children: [
          {
            path: 'visitor',
            name: 'VisitorList',
            component: () => import('@/views/security/VisitorList.vue'),
            meta: { title: '访客管理' }
          },
          {
            path: 'visitor-type',
            name: 'VisitorType',
            component: () => import('@/views/security/VisitorType.vue'),
            meta: { title: '访客类型' }
          },
          {
            path: 'car',
            name: 'CarList',
            component: () => import('@/views/security/CarList.vue'),
            meta: { title: '车辆管理' }
          },
          {
            path: 'car-history',
            name: 'CarHistory',
            component: () => import('@/views/security/CarHistory.vue'),
            meta: { title: '通行记录' }
          },
          {
            path: 'gate',
            name: 'GateList',
            component: () => import('@/views/security/GateList.vue'),
            meta: { title: '门岗管理' }
          }
        ]
      },
      {
        path: 'facility',
        name: 'FacilityManage',
        redirect: '/facility/list',
        children: [
          {
            path: 'list',
            name: 'FacilityList',
            component: () => import('@/views/facility/FacilityList.vue'),
            meta: { title: '设施信息' }
          },
          {
            path: 'type',
            name: 'FacilityType',
            component: () => import('@/views/facility/FacilityType.vue'),
            meta: { title: '设施类型' }
          }
        ]
      },
      {
        path: 'asset',
        name: 'AssetManage',
        redirect: '/asset/property',
        children: [
          {
            path: 'property',
            name: 'PropertyList',
            component: () => import('@/views/asset/PropertyList.vue'),
            meta: { title: '房产管理' }
          },
          {
            path: 'parking',
            name: 'ParkingList',
            component: () => import('@/views/asset/ParkingList.vue'),
            meta: { title: '停车位管理' }
          }
        ]
      },
      {
        path: 'task',
        name: 'TaskManage',
        redirect: '/task/list',
        children: [
          {
            path: 'list',
            name: 'TaskList',
            component: () => import('@/views/task/TaskList.vue'),
            meta: { title: '工单列表' }
          },
          {
            path: 'type',
            name: 'TaskType',
            component: () => import('@/views/task/TaskType.vue'),
            meta: { title: '工单类型' }
          }
        ]
      },
      {
        path: 'settings',
        name: 'UserSettings',
        component: () => import('@/views/UserSettings.vue'),
        meta: { title: '个人设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register']
  const authRequired = !publicPages.includes(to.path)
  const userStore = useUserStore()

  if (authRequired && !userStore.isLoggedIn) {
    next('/login')
  } else if (!authRequired && userStore.isLoggedIn) {
    next('/welcome')
  } else {
    next()
  }
})

export default router
