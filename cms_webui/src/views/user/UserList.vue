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
          <el-input v-model="query.userPhone" placeholder="电话" maxlength="11" clearable @input="handleQueryPhoneInput" />
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
      <el-button @click="fetchData(true)">刷新</el-button>
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
        <el-table-column prop="id" label="ID" width="100" sortable show-overflow-tooltip />
        <el-table-column label="头像" width="60">
          <template #default="{ row }">
            <el-avatar
              :size="32"
              shape="circle"
              :src="userAvatar(row)"
              class="user-avatar"
              :style="{ backgroundColor: 'var(--vp-c-brand-1)' }"
            >
              {{ avatarInitial(row) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="账户" width="140" sortable show-overflow-tooltip />
        <el-table-column prop="userName" label="用户名" width="140" show-overflow-tooltip />
        <el-table-column prop="roleName" label="角色" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag effect="plain" type="success">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="年龄" width="80" sortable show-overflow-tooltip>
          <template #default="{ row }">
            {{ userAge(row.userBirthday) }}
          </template>
        </el-table-column>
        <el-table-column prop="userPhone" label="电话" width="160" show-overflow-tooltip />
        <el-table-column prop="userEmail" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="userGender" label="性别" width="60" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.userGender === 0 ? '男' : row.userGender === 1 ? '女' : row.userGender === 2 ? '不愿透露' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="userStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="userStatusType(row.userStatus)" effect="plain">
              {{ userStatusText(row.userStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCreateTime" label="创建时间" width="160" show-overflow-tooltip />
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
            <el-popconfirm title="确定删除该用户记录吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" v-if="userStore.userRole === 0 && userStore.canOperate" @click.stop="() => {}">
              删除
            </el-button>
              </template>
            </el-popconfirm>
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
          <el-form-item label="用户头像" class="avatar-form-item">
            <div class="avatar-form-row">
              <AvatarEditor v-model="form.userAvata" :initial="avatarInitial(form)" :size="96" @uploaded="handleFormAvatarUploaded" />
            </div>
          </el-form-item>

          <el-form-item label="账户" prop="uid">
            <el-input v-model="form.uid" placeholder="账户将用于登录，不可重复" :disabled="isEdit && !isSystemAdmin" />
          </el-form-item>
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" placeholder="请输入用户名"/>
          </el-form-item>
          <el-form-item label="性别" prop="userGender">
            <el-radio-group v-model="form.userGender">
              <el-radio :value="0">男</el-radio>
              <el-radio :value="1">女</el-radio>
              <el-radio :value="2">不愿透露</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="电话" prop="userPhone">
            <el-input v-model="form.userPhone" maxlength="11" placeholder="请输入手机号" clearable @input="handleFormPhoneInput" />
          </el-form-item>
          <el-form-item label="邮箱" prop="userEmail">
            <el-input v-model="form.userEmail" maxlength="64" placeholder="请输入邮箱地址" clearable />
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
            <el-select v-model="form.userRole" placeholder="请选择角色" :disabled="isEdit && !isSystemAdmin">
              <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态设置" prop="userStatus">
            <el-select v-model="form.userStatus" placeholder="请选择状态" :disabled="isEdit && !isSystemAdmin">
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
          <ConfirmInfoTable :rows="confirmRows" />
        </section>
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserPage, getUserRoles, addUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'
import AvatarEditor from '@/components/AvatarEditor.vue'
import { genderRequiredRule, optionalEmailRule, optionalPhoneRule, sanitizePhone } from '@/utils/validators'

const userStore = useUserStore()
const canEditSelf = computed(() => userStore.canOperate)
const isSystemAdmin = computed(() => Number(userStore.userRole) === userStore.ROLE_ADMIN)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roles = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

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
  { title: '基本信息', fields: ['uid', 'userName', 'userGender', 'userPhone', 'userEmail'] },
  { title: '用户配置', fields: ['userRole', 'userStatus', 'userPassword', 'confirmPassword'] },
  { title: '确认信息', fields: [] }
]
const editWizardSteps = [
  { title: '基本信息', fields: ['uid', 'userName', 'userGender', 'userPhone', 'userEmail'] },
  { title: '用户配置', fields: ['userRole', 'userStatus', 'userPassword', 'confirmPassword'] },
  { title: '确认信息', fields: [] }
]

const confirmRows = computed(() => [
  { label: 'ID', value: valueText(form.value.id) },
  {
    label: '头像',
    type: 'avatar',
    avatarUrl: form.value.userAvata || form.value.userAvatar || '',
    initial: avatarInitial(form.value)
  },
  { label: '账户', value: valueText(form.value.uid) },
  { label: '用户名', value: valueText(form.value.userName) },
  { label: '性别', value: genderText(form.value.userGender) },
  { label: '电话', value: valueText(form.value.userPhone) },
  { label: '邮箱', value: valueText(form.value.userEmail) },
  { label: '生日', value: valueText(form.value.userBirthday) },
  { label: '年龄', value: userAge(form.value.userBirthday) },
  { label: '用户角色', type: 'tag', tagType: 'success', value: roleNameText(form.value.userRole) },
  { label: '状态设置', type: 'tag', tagType: userStatusType(form.value.userStatus), value: userStatusText(form.value.userStatus) },
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
  userPhone: [optionalPhoneRule()],
  userEmail: [optionalEmailRule()],
  userGender: [genderRequiredRule()],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  userStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function handleQueryPhoneInput(value) {
  query.value.userPhone = sanitizePhone(value)
}

function handleFormPhoneInput(value) {
  form.value.userPhone = sanitizePhone(value)
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const params = { ...query.value }
    Object.keys(params).forEach(k => { if (params[k] === '' || params[k] === undefined) delete params[k] })
    const res = await getUserPage(params, { forceRefresh: !!forceRefresh })
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
  fetchData(true)
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
    userGender: undefined
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
    userGender: row.userGender ?? undefined
  }
}

async function handleFormAvatarUploaded(url) {
  form.value.userAvata = url
  if (!isEdit.value || !form.value.id) return
  await updateUser({
    id: form.value.id,
    userName: form.value.userName,
    userBirthday: form.value.userBirthday || null,
    userGender: form.value.userGender,
    userPhone: form.value.userPhone,
    userEmail: form.value.userEmail,
    userAvata: url
  })
  const row = tableData.value.find(item => item.id === form.value.id)
  if (row) row.userAvata = url
  ElMessage.success('头像已更新')
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
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData(true)
}

async function setStatus(row, userStatus) {
  await updateUserStatus({ id: row.id, userStatus })
  ElMessage.success('操作成功')
  fetchData(true)
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

function genderText(gender) {
  return gender === 0 ? '男' : gender === 1 ? '女' : gender === 2 ? '不愿透露' : '-'
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
  fetchData(true)
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
.avatar-form-item :deep(.el-form-item__label) {
  display: flex;
  align-items: center;
  align-self: stretch;
}
.avatar-form-row {
  display: flex;
  justify-content: center;
  width: 100%;
}
@media (max-width: 640px) {
  .avatar-form-row {
    justify-content: center;
  }

  .avatar-form-row :deep(.avatar-editor) {
    width: 100%;
  }
}
</style>
