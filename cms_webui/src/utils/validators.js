export const phonePattern = /^1[3-9]\d{9}$/

export const emailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

export const captchaPattern = /^[A-Za-z0-9]{4}$/

export function sanitizePhone(value) {
  return String(value || '').replace(/\D/g, '').slice(0, 11)
}

export function captchaInputValidator(value) {
  return /^[A-Za-z0-9]*$/.test(String(value || ''))
}

export function captchaRule(message = '请输入验证码') {
  return {
    validator: (rule, value, callback) => {
      if (captchaPattern.test(String(value || ''))) {
        callback()
        return
      }
      callback(new Error(message))
    },
    trigger: 'blur'
  }
}

export function genderRequiredRule(message = '请选择性别') {
  return {
    validator: (rule, value, callback) => {
      if (value === 0 || value === 1 || value === 2) {
        callback()
        return
      }
      callback(new Error(message))
    },
    trigger: 'change'
  }
}

export function optionalPhoneRule(message = '请输入正确的手机号') {
  return {
    validator: (rule, value, callback) => {
      if (!value || phonePattern.test(String(value).trim())) {
        callback()
        return
      }
      callback(new Error(message))
    },
    trigger: 'blur'
  }
}

export function optionalEmailRule(message = '请输入正确的邮箱地址') {
  return {
    validator: (rule, value, callback) => {
      if (!value || emailPattern.test(String(value).trim())) {
        callback()
        return
      }
      callback(new Error(message))
    },
    trigger: 'blur'
  }
}
