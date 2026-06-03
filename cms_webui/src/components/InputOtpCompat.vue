<template>
  <div class="otp-input" @paste.prevent="handlePaste">
    <input
      v-for="(_, index) in boxes"
      :key="index"
      ref="inputRefs"
      class="otp-input__box"
      :value="chars[index] || ''"
      inputmode="none"
      :maxlength="1"
      autocomplete="one-time-code"
      @input="handleInput(index, $event)"
      @keydown="handleKeydown(index, $event)"
      @focus="$event.target.select()"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  length: { type: Number, default: 6 },
  inputmode: { type: String, default: 'numeric' },
  validator: { type: Function, default: null }
})

const emit = defineEmits(['update:modelValue'])
const inputRefs = ref([])
const boxes = computed(() => Array.from({ length: props.length }))
const chars = computed(() => String(props.modelValue || '').slice(0, props.length).split(''))

function isAllowed(value) {
  return !props.validator || props.validator(value)
}

function normalize(value) {
  return String(value || '')
    .split('')
    .map(char => char.toUpperCase())
    .filter(char => isAllowed(char))
    .join('')
    .slice(0, props.length)
}

function focusInput(index) {
  nextTick(() => inputRefs.value[index]?.focus?.())
}

function updateAt(index, value) {
  const next = chars.value.slice()
  next[index] = value
  emit('update:modelValue', next.join('').slice(0, props.length))
}

function handleInput(index, event) {
  const value = normalize(event.target.value)
  if (!value) {
    updateAt(index, '')
    return
  }

  if (value.length > 1) {
    const next = chars.value.slice()
    value.split('').forEach((char, offset) => {
      if (index + offset < props.length) next[index + offset] = char
    })
    emit('update:modelValue', next.join('').slice(0, props.length))
    focusInput(Math.min(index + value.length, props.length - 1))
    return
  }

  updateAt(index, value)
  if (index < props.length - 1) focusInput(index + 1)
}

function handleKeydown(index, event) {
  if (event.key === 'Backspace' && !chars.value[index] && index > 0) {
    updateAt(index - 1, '')
    focusInput(index - 1)
  }
  if (event.key === 'ArrowLeft' && index > 0) {
    event.preventDefault()
    focusInput(index - 1)
  }
  if (event.key === 'ArrowRight' && index < props.length - 1) {
    event.preventDefault()
    focusInput(index + 1)
  }
}

function handlePaste(event) {
  const value = normalize(event.clipboardData?.getData('text'))
  emit('update:modelValue', value)
  focusInput(Math.min(value.length, props.length - 1))
}
</script>

<style scoped>
.otp-input {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.otp-input__box {
  width: 40px;
  height: 40px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  color: var(--vp-c-text-1);
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
  text-align: center;
  text-transform: uppercase;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.otp-input__box:focus {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-9);
}
</style>
