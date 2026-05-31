<template>
  <el-drawer
    :model-value="modelValue"
    direction="rtl"
    :size="drawerSize"
    :with-header="false"
    :before-close="handleBeforeClose"
    class="wizard-form-drawer"
    @closed="handleClosed"
  >
    <div class="wizard-panel">
      <header class="wizard-header">
        <div>
          <p class="wizard-kicker">{{ mode === 'edit' ? '编辑信息' : '新增信息' }}</p>
          <h3>{{ title }}</h3>
        </div>
        <el-button circle text @click="requestClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </header>

      <el-steps :active="activeStep" finish-status="success" align-center class="wizard-steps">
        <el-step v-for="step in normalizedSteps" :key="step.title" :title="step.title" />
      </el-steps>

      <main class="wizard-body">
        <el-form ref="formRef" :model="model" :rules="rules" :label-width="labelWidth">
          <slot :active-step="activeStep" :mode="mode" />
        </el-form>
      </main>

      <footer class="wizard-footer">
        <el-button @click="requestClose">取消</el-button>
        <el-button :disabled="activeStep === 0" @click="prevStep">上一步</el-button>
        <el-button v-if="!isLastStep" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-else type="primary" :loading="loading" @click="submitForm">保存</el-button>
      </footer>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  mode: { type: String, default: 'add' },
  steps: { type: Array, default: () => [] },
  model: { type: Object, required: true },
  rules: { type: Object, default: () => ({}) },
  labelWidth: { type: String, default: '88px' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel', 'closed'])

const activeStep = ref(0)
const formRef = ref(null)
const initialSnapshot = ref('')
const drawerSize = computed(() => (window.innerWidth <= 768 ? '100%' : '640px'))
const normalizedSteps = computed(() => {
  if (props.steps.length >= 2) return props.steps
  return [{ title: '基础信息', fields: [] }, { title: '确认提交', fields: [] }]
})
const isLastStep = computed(() => activeStep.value === normalizedSteps.value.length - 1)

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      activeStep.value = 0
      initialSnapshot.value = snapshotModel()
      formRef.value?.clearValidate?.()
    }
  }
)

function snapshotModel() {
  try {
    return JSON.stringify(props.model)
  } catch {
    return ''
  }
}

function isDirty() {
  return props.modelValue && initialSnapshot.value && initialSnapshot.value !== snapshotModel()
}

async function validateFields(fields) {
  if (!formRef.value) return true
  if (!fields || fields.length === 0) return true
  await formRef.value.validateField(fields)
  return true
}

async function nextStep() {
  const fields = normalizedSteps.value[activeStep.value]?.fields || []
  await validateFields(fields)
  activeStep.value += 1
}

function prevStep() {
  if (activeStep.value > 0) activeStep.value -= 1
}

async function submitForm() {
  await formRef.value?.validate()
  emit('submit')
}

function closeDrawer() {
  emit('update:modelValue', false)
}

async function requestClose() {
  if (isDirty()) {
    await ElMessageBox.confirm('表单内容尚未保存，确定关闭吗？', '提示', { type: 'warning' })
  }
  emit('cancel')
  closeDrawer()
}

function handleBeforeClose(done) {
  if (!isDirty()) {
    done()
    return
  }
  ElMessageBox.confirm('表单内容尚未保存，确定关闭吗？', '提示', { type: 'warning' })
    .then(() => {
      emit('cancel')
      done()
    })
    .catch(() => {})
}

function handleClosed() {
  emit('closed')
}
</script>

<style scoped>
.wizard-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.wizard-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--vp-c-divider);
}

.wizard-kicker {
  margin: 0 0 6px;
  color: var(--vp-c-brand-1);
  font-size: 12px;
  font-weight: 700;
}

.wizard-header h3 {
  margin: 0;
  color: var(--vp-c-text-1);
  font-size: 20px;
}

.wizard-steps {
  padding: 22px 0 18px;
}

.wizard-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 4px 2px 18px;
}

.wizard-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid var(--vp-c-divider);
}

@media (max-width: 768px) {
  .wizard-steps {
    padding: 18px 0 14px;
  }

  .wizard-footer {
    flex-wrap: wrap;
  }

  .wizard-footer .el-button {
    flex: 1;
    margin-left: 0;
  }
}
</style>
