<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限管理</h2>
    </div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit">
        <el-icon><Plus /></el-icon> 新增角色组
      </el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="roleId" label="角色ID" width="160" show-overflow-tooltip />
        <el-table-column prop="roleName" label="角色名称" min-width="300" show-overflow-tooltip />
        <el-table-column label="权限等级" width="400">
          <template #default="{ row }">
            <el-tag effect="plain" :type="permissionTagType(row.rolePromission)">
              {{ permissionText(row.rolePromission) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该角色组吗？" confirm-button-text="删除" cancel-button-text="取消" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small" @click.stop="() => {}">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <WizardFormDrawer
      v-model="dialogVisible"
      title="新增角色组"
      mode="add"
      :steps="wizardSteps"
      :model="form"
      :rules="formRules"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <template #default="{ activeStep }">
        <template v-if="activeStep === 0">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="权限等级" prop="rolePromission">
          <el-select v-model="form.rolePromission" placeholder="请选择">
            <el-option label="0 - 只读权限" value="0" />
            <el-option label="1 - 操作权限" value="1" />
            <el-option label="2 - 管理权限" value="2" />
            <el-option label="3 - 系统权限" value="3" />
          </el-select>
        </el-form-item>
        </template>
        <ConfirmInfoTable v-else :rows="confirmRows" />
      </template>
    </WizardFormDrawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserRoles, addUserRole, deleteUserRole } from '@/api/user'
import WizardFormDrawer from '@/components/WizardFormDrawer.vue'
import ConfirmInfoTable from '@/components/ConfirmInfoTable.vue'

const userStore = useUserStore()
const canEdit = computed(() => userStore.userRole === 0 && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const form = ref({ roleName: '', rolePromission: '1' })
const wizardSteps = [
  { title: '基础信息', fields: ['roleName', 'rolePromission'] },
  { title: '确认信息', fields: [] }
]
const confirmRows = computed(() => [
  { label: '角色名称', value: valueText(form.value.roleName) },
  { label: '权限等级', type: 'tag', tagType: permissionTagType(form.value.rolePromission), value: permissionText(form.value.rolePromission) }
])
const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  rolePromission: [{ required: true, message: '请选择权限等级', trigger: 'change' }]
}

function valueText(value) {
  return value === '' || value === undefined || value === null ? '-' : value
}

async function fetchData(forceRefresh = false) {
  loading.value = true
  try {
    const res = await getUserRoles({ forceRefresh: !!forceRefresh })
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}


function openAdd() {
  form.value = { roleName: '', rolePromission: '1' }
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await addUserRole(form.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData(true)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteUserRole(row.roleId)
  ElMessage.success('删除成功')
  fetchData(true)
}

function permissionText(level) {
  return {
    '0': '只读权限',
    '1': '操作权限',
    '2': '管理权限',
    '3': '系统权限'
  }[String(level)] || `等级 ${level}`
}

function permissionTagType(level) {
  return {
    '0': 'info',
    '1': 'success',
    '2': 'warning',
    '3': 'danger'
  }[String(level)] || 'info'
}
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
.table-toolbar { margin-bottom: 12px; }
</style>
