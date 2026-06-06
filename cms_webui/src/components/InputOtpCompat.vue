<template>
  <div class="otp-input" @paste.prevent="handlePaste">
    <input
      v-for="(_, index) in boxes"
      :key="index"
      ref="inputRefs"
      class="otp-input__box"
      :value="chars[index] || ''"
      type="text"
      :inputmode="inputmode"
      :maxlength="length"
      autocomplete="one-time-code"
      autocapitalize="characters"
      autocorrect="off"
      spellcheck="false"
      enterkeyhint="done"
      @beforeinput="handleBeforeInput(index, $event)"
      @input="handleInput(index, $event)"
      @keydown="handleKeydown(index, $event)"
      @compositionstart="isComposing = true"
      @compositionend="handleCompositionEnd(index, $event)"
      @focus="handleFocus"
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
const isComposing = ref(false)
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

function blurInput(index) {
  nextTick(() => inputRefs.value[index]?.blur?.())
}

function handleFocus(event) {
  nextTick(() => event.target?.select?.())
}

function updateAt(index, value) {
  const next = chars.value.slice()
  next[index] = value
  emit('update:modelValue', next.join('').slice(0, props.length))
}

function applyValue(index, rawValue) {
  const value = normalize(rawValue)
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
    const nextIndex = Math.min(index + value.length, props.length - 1)
    if (index + value.length >= props.length) {
      blurInput(nextIndex)
    } else {
      focusInput(nextIndex)
    }
    return
  }

  updateAt(index, value)
  if (index < props.length - 1) {
    focusInput(index + 1)
  } else {
    blurInput(index)
  }
}

function handleBeforeInput(index, event) {
  if (event.inputType !== 'insertText' || !event.data || event.isComposing || isComposing.value) return
  event.preventDefault()
  applyValue(index, event.data)
}

function handleInput(index, event) {
  if (isComposing.value || event.isComposing) return
  applyValue(index, event.target.value)
}

function handleCompositionEnd(index, event) {
  isComposing.value = false
  applyValue(index, event.data || event.target.value)
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
