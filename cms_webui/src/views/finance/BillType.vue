<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">账单类型</h2>
    </div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit">
        <el-icon><Plus /></el-icon> 新增类型
      </el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="billTypeId" label="类型ID" width="100" />
        <el-table-column prop="billTypeName" label="类型名称" min-width="200" />
        <el-table-column label="操作" width="100" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增账单类型" width="420" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="类型名称" prop="billTypeName">
          <el-input v-model="form.billTypeName" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getBillTypes, addBillType, deleteBillType } from '@/api/bill'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({ billTypeName: '' })
const formRules = {
  billTypeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getBillTypes()
    tableData.value = res.data
  } finally { loading.value = false }
}

function openAdd() {
  form.value = { billTypeName: '' }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addBillType(form.value)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除账单类型 ${row.billTypeName} 吗？`, '提示', { type: 'warning' })
  await deleteBillType(row.billTypeId)
  ElMessage.success('删除成功')
  fetchData()
}
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
