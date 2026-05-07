<template>
  <div class="page-container">
    <div class="page-header"><h2 class="page-title">门岗管理</h2></div>
    <div class="table-toolbar">
      <el-button type="primary" @click="openAdd" v-if="canEdit"><el-icon><Plus /></el-icon> 新增门岗</el-button>
    </div>
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gateCode" label="门岗编号" width="140" />
        <el-table-column prop="gateName" label="门岗名称" width="160" />
        <el-table-column prop="chiefName" label="负责人" width="120" />
        <el-table-column prop="gateRemark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" v-if="canEdit">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑门岗' : '新增门岗'" width="480" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="gateName"><el-input v-model="form.gateName" /></el-form-item>
        <el-form-item label="负责人ID" prop="gateChiefId"><el-input v-model.number="form.gateChiefId" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.gateRemark" type="textarea" :rows="2" /></el-form-item>
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
import { getGateList, addGate, updateGate, deleteGate } from '@/api/gate'

const userStore = useUserStore()
const canEdit = computed(() => [0, 1].includes(userStore.userRole) && userStore.canOperate)
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({})
const formRules = { gateName: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

async function fetchData() { loading.value = true; try { const res = await getGateList(); tableData.value = res.data } finally { loading.value = false } }
function openAdd() { isEdit.value = false; form.value = { gateName: '', gateChiefId: '', gateRemark: '' }; dialogVisible.value = true }
function openEdit(row) { isEdit.value = true; form.value = { id: row.id, gateName: row.gateName, gateChiefId: row.gateChiefId, gateRemark: row.gateRemark }; dialogVisible.value = true }
async function handleSubmit() { await formRef.value.validate(); submitLoading.value = true; try { if (isEdit.value) { await updateGate(form.value); ElMessage.success('更新成功') } else { await addGate(form.value); ElMessage.success('添加成功') }; dialogVisible.value = false; fetchData() } finally { submitLoading.value = false } }
async function handleDelete(row) { await ElMessageBox.confirm('确定删除?', '提示', { type: 'warning' }); await deleteGate(row.id); ElMessage.success('删除成功'); fetchData() }
onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 20px; font-weight: 700; color: var(--vp-c-text-1); margin: 0; }
.table-toolbar { margin-bottom: 12px; }
.table-card { border-radius: 8px; border: 1px solid var(--vp-c-divider); }
</style>
