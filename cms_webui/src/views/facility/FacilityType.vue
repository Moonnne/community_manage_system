<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">设施类型</h2></div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增类型</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="类型ID" width="100" />
        <el-table-column prop="facilityTypeName" label="类型名称" min-width="200" />
        <el-table-column prop="facilityTypeRemark" label="备注" min-width="300" />
        <el-table-column label="操作" width="100" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增设施类型" width="420" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="facilityTypeName"><el-input v-model="form.facilityTypeName" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.facilityTypeRemark" type="textarea" :rows="2" /></el-form-item>
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
import { getFacilityTypes, addFacilityType, deleteFacilityType } from '@/api/facility'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({})
const formRules = { facilityTypeName: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

async function fetchData() { loading.value = true; try { const res = await getFacilityTypes(); tableData.value = res.data } finally { loading.value = false } }
function openAdd() { form.value = { facilityTypeName: '', facilityTypeRemark: '' }; dialogVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); submitLoading.value = true; try { await addFacilityType(form.value); ElMessage.success('添加成功'); dialogVisible.value = false; fetchData() } finally { submitLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteFacilityType(row.id); ElMessage.success('删除成功'); fetchData() }
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
