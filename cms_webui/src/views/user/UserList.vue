<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">用户信息</h2>
    </div>

    <!-- Search -->
    <el-card shadow="never" class="search-card">
      <el-row :gutter="16">
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-input v-model="query.uid" placeholder="账户" clearable />
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-input v-model="query.userName" placeholder="用户名" clearable />
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-input v-model="query.userPhone" placeholder="电话" clearable />
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-select v-model="query.userRole" placeholder="角色" clearable>
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-select v-model="query.userStatus" placeholder="状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="冻结" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="4" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Toolbar -->
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="userStore.userRole === 0 && userStore.canOperate">
        <el-icon><Plus /></el-icon> 添加用户
      </el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        style="width: 100%"
        height="410"
      >
        <el-table-column prop="uid" label="账户" width="120" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag effect="plain" type="success">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userPhone" label="电话" width="140" />
        <el-table-column prop="userEmail" label="邮箱" min-width="180" />
        <el-table-column prop="userGender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.userGender === 1 ? '男' : row.userGender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="userStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="userStatusType(row.userStatus)" effect="plain">
              {{ userStatusText(row.userStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCreateTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)" v-if="userStore.canOperate">编辑</el-button>
            <el-button link type="primary" size="small" @click="setStatus(row, 0)" v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate && row.userStatus !== 0">
              恢复
            </el-button>
            <el-button link type="warning" size="small" @click="setStatus(row, 1)" v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate && row.userStatus !== 1">
              冻结
            </el-button>
            <el-button link type="danger" size="small" @click="setStatus(row, 2)" v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate && row.userStatus !== 2">
              禁用
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" v-if="userStore.userRole === 0 && userStore.canOperate">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="520" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="账户" prop="uid">
          <el-input v-model="form.uid" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword" v-if="!isEdit">
          <el-input v-model="form.userPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="userRole" v-if="!isEdit">
          <el-select v-model="form.userRole" placeholder="请选择角色">
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="userStatus" v-if="!isEdit">
          <el-select v-model="form.userStatus" placeholder="请选择状态">
            <el-option label="正常" :value="0" />
            <el-option label="冻结" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话" prop="userPhone">
          <el-input v-model="form.userPhone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="userEmail">
          <el-input v-model="form.userEmail" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.userGender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserPage, getUserRoles, addUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'

const userStore = useUserStore()
const canEditSelf = computed(() => userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const query = ref({
  pageNum: 1,
  pageSize: 10,
  uid: '',
  userName: '',
  userPhone: '',
  userRole: undefined,
  userStatus: undefined
})

const form = ref({})

const formRules = {
  uid: [{ required: true, message: '请输入账户', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query.value }
    Object.keys(params).forEach(k => { if (params[k] === '' || params[k] === undefined) delete params[k] })
    const res = await getUserPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  const res = await getUserRoles()
  roles.value = res.data
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, uid: '', userName: '', userPhone: '', userRole: undefined, userStatus: undefined }
  fetchData()
}

function openAdd() {
  isEdit.value = false
  form.value = { uid: '', userName: '', userPassword: '', userRole: undefined, userStatus: 0, userPhone: '', userEmail: '', userGender: 1 }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = { id: row.id, userName: row.userName, userPhone: row.userPhone, userEmail: row.userEmail, userGender: row.userGender }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!canEditSelf.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.value)
      ElMessage.success('更新成功')
    } else {
      await addUser(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户 ${row.userName} ?`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

async function setStatus(row, userStatus) {
  await updateUserStatus({ id: row.id, userStatus })
  ElMessage.success('操作成功')
  fetchData()
}

function userStatusText(status) {
  return ['正常', '冻结', '禁用'][status] || '未知'
}

function userStatusType(status) {
  return ['success', 'warning', 'danger'][status] || 'info'
}

onMounted(() => {
  fetchData()
  fetchRoles()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
