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
        <el-table-column prop="id" label="ID" width="100" sortable />
        <el-table-column label="头像" width="60">
          <template #default="{ row }">
            <el-avatar :size="32" shape="circle" :src="userAvatar(row)">
              {{ avatarInitial(row) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="账户" width="140" sortable />
        <el-table-column prop="userName" label="用户名" width="140" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag effect="plain" type="success">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="年龄" width="80" sortable>
          <template #default="{ row }">
            {{ userAge(row.userBirthday) }}
          </template>
        </el-table-column>
        <el-table-column prop="userPhone" label="电话" width="160" />
        <el-table-column prop="userEmail" label="邮箱" min-width="200" />
        <el-table-column prop="userGender" label="性别" width="60">
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
        <el-table-column prop="userCreateTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)" v-if="userStore.canOperate">编辑</el-button>
            <el-button link type="primary" size="small" @click="setStatus(row, 0)" v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate && row.userStatus === 1">
              恢复
            </el-button>
            <el-button
              link
              type="warning"
              size="small"
              :disabled="row.userStatus === 2"
              @click="setStatus(row, 1)"
              v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate && row.userStatus !== 1"
            >
              冻结
            </el-button>
            <el-button
              link
              :type="row.userStatus === 2 ? 'primary' : 'danger'"
              size="small"
              @click="setStatus(row, row.userStatus === 2 ? 0 : 2)"
              v-if="[0, 1].includes(userStore.userRole) && userStore.canOperate"
            >
              {{ row.userStatus === 2 ? '恢复' : '禁用' }}
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
    <WizardFormDrawer
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      :mode="isEdit ? 'edit' : 'add'"
      :steps="isEdit ? editWizardSteps : addWizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <section v-show="activeStep === 0" class="user-wizard-step">
          <div class="avatar-editor">
            <el-upload
              class="avatar-uploader"
              accept="image/*"
              :show-file-list="false"
              :http-request="uploadAvatarRequest"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar :size="128" shape="circle" :src="form.userAvata || form.userAvatar">
                {{ avatarInitial(form) }}
              </el-avatar>
              <div class="avatar-upload-mask">
                <el-icon><UploadFilled /></el-icon>
                <span>{{ avatarUploading ? '上传中' : '上传头像' }}</span>
              </div>
            </el-upload>
            <div class="avatar-editor-text">
              <h4>用户头像</h4>
              <p>选择图片后会在前端居中裁剪为 128 x 128，再上传到 OSS。</p>
            </div>
          </div>

          <el-form-item label="ID">
            <el-input v-model="form.id" disabled placeholder="保存后自动生成" />
          </el-form-item>
          <el-form-item label="账户" prop="uid">
            <el-input v-model="form.uid" :disabled="isEdit" />
          </el-form-item>
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.userGender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="电话" prop="userPhone">
            <el-input v-model="form.userPhone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="userEmail">
            <el-input v-model="form.userEmail" />
          </el-form-item>
          <el-form-item label="年龄">
            <el-date-picker
              v-model="form.userBirthday"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择生日"
            />
          </el-form-item>
        </section>

        <section v-show="activeStep === 1" class="user-wizard-step">
          <el-form-item label="用户角色" prop="userRole">
            <el-select v-model="form.userRole" placeholder="请选择角色" :disabled="isEdit">
              <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态设置" prop="userStatus">
            <el-select v-model="form.userStatus" placeholder="请选择状态" :disabled="isEdit">
              <el-option label="正常" :value="0" />
              <el-option label="冻结" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-input v-model="form.userCreateTime" disabled placeholder="保存后自动生成" />
          </el-form-item>
          <el-form-item label="用户密码" prop="userPassword">
            <el-input
              v-model="form.userPassword"
              type="password"
              show-password
              :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'"
            />
          </el-form-item>
          <el-form-item v-if="form.userPassword" label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password />
          </el-form-item>
        </section>

        <section v-show="activeStep === 2" class="user-wizard-step">
          <el-table :data="confirmRows" stripe style="width: 100%">
            <el-table-column prop="label" label="字段" width="140" />
            <el-table-column prop="value" label="内容" min-width="220" />
          </el-table>
        </section>
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserPage, getUserRoles, addUser, updateUser, deleteUser, updateUserStatus, uploadUserAvatar } from '@/api/user'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'

const userStore = useUserStore()
const canEditSelf = computed(() => userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const avatarUploading = ref(false)

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
const addWizardSteps = [
  { title: '基本信息', fields: ['uid', 'userName'] },
  { title: '用户配置', fields: ['userRole', 'userStatus', 'userPassword', 'confirmPassword'] },
  { title: '确认信息', fields: [] }
]
const editWizardSteps = [
  { title: '基本信息', fields: ['uid', 'userName'] },
  { title: '用户配置', fields: ['userPassword', 'confirmPassword'] },
  { title: '确认信息', fields: [] }
]

const confirmRows = computed(() => [
  { label: 'ID', value: valueText(form.value.id) },
  { label: '头像', value: valueText(form.value.userAvata || form.value.userAvatar) },
  { label: '账户', value: valueText(form.value.uid) },
  { label: '用户名', value: valueText(form.value.userName) },
  { label: '性别', value: form.value.userGender === 1 ? '男' : form.value.userGender === 2 ? '女' : '-' },
  { label: '电话', value: valueText(form.value.userPhone) },
  { label: '邮箱', value: valueText(form.value.userEmail) },
  { label: '生日', value: valueText(form.value.userBirthday) },
  { label: '年龄', value: userAge(form.value.userBirthday) },
  { label: '用户角色', value: roleNameText(form.value.userRole) },
  { label: '状态设置', value: userStatusText(form.value.userStatus) },
  { label: '创建时间', value: valueText(form.value.userCreateTime) },
  { label: '用户密码', value: form.value.userPassword ? '已设置' : '未修改' }
])

function validatePassword(rule, value, callback) {
  if (!isEdit.value && !value) {
    callback(new Error('请输入密码'))
    return
  }
  callback()
}

function validateConfirmPassword(rule, value, callback) {
  if (form.value.userPassword && value !== form.value.userPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const formRules = {
  uid: [{ required: true, message: '请输入账户', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  userPassword: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  userStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
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
  form.value = getDefaultForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = mapRowToForm(row)
  dialogVisible.value = true
}

function getDefaultForm() {
  return {
    id: '',
    uid: '',
    userName: '',
    userAvata: '',
    userBirthday: '',
    userPassword: '',
    confirmPassword: '',
    userRole: undefined,
    userStatus: 0,
    userCreateTime: '',
    userPhone: '',
    userEmail: '',
    userGender: 1
  }
}

function mapRowToForm(row) {
  return {
    id: row.id,
    uid: row.uid,
    userName: row.userName,
    userAvata: row.userAvata || row.userAvatar || '',
    userBirthday: row.userBirthday || '',
    userPassword: '',
    confirmPassword: '',
    userRole: row.userRole,
    userStatus: row.userStatus,
    userCreateTime: row.userCreateTime || '',
    userPhone: row.userPhone,
    userEmail: row.userEmail,
    userGender: row.userGender
  }
}

async function handleSubmit() {
  if (!canEditSelf.value) return
  submitLoading.value = true
  try {
    const submitForm = { ...form.value }
    delete submitForm.confirmPassword
    if (isEdit.value && !submitForm.userPassword) delete submitForm.userPassword
    if (isEdit.value) {
      await updateUser(submitForm)
      ElMessage.success('更新成功')
    } else {
      await addUser(submitForm)
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

function avatarInitial(row) {
  return (row.userName || row.uid || 'U').charAt(0).toUpperCase()
}

function valueText(value) {
  return value === '' || value === undefined || value === null ? '-' : value
}

function roleNameText(roleId) {
  return roles.value.find(role => role.roleId === roleId)?.roleName || valueText(roleId)
}

function userStatusText(status) {
  return ['正常', '冻结', '禁用'][status] || '未知'
}

function userStatusType(status) {
  return ['success', 'warning', 'danger'][status] || 'info'
}

function userAvatar(row) {
  return row.userAvata || row.userAvatar || ''
}

function userAge(birthday) {
  if (!birthday) return '-'

  const birthDate = new Date(`${birthday}T00:00:00`)
  if (Number.isNaN(birthDate.getTime())) return '-'

  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  const dayDiff = today.getDate() - birthDate.getDate()

  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age -= 1
  }

  return age >= 0 ? age : '-'
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
.user-wizard-step {
  min-height: 420px;
}
.avatar-editor {
  display: grid;
  grid-template-columns: 128px 1fr;
  gap: 18px;
  align-items: center;
  margin-bottom: 18px;
}
.avatar-editor :deep(.el-form-item) {
  margin-bottom: 0;
}
@media (max-width: 640px) {
  .avatar-editor {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .avatar-editor :deep(.el-form-item) {
    width: 100%;
  }
}
</style>
