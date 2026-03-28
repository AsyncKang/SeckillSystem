<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'

const router = useRouter()
const username = ref('')
const password = ref('')
const phone = ref('')
const loading = ref(false)

async function submit() {
  if (!username.value || !password.value) {
    alert('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const { data } = await userApi.register({ username: username.value, password: password.value, phone: phone.value })
    localStorage.setItem('token', data.token)
    const { data: me } = await userApi.me()
    localStorage.setItem('user', JSON.stringify(me))
    router.push('/')
  } catch (e) {
    alert(e.response?.data?.message || e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="box">
      <h1>注册</h1>
      <p class="subtitle">Create your account. 一键完成注册</p>
      <form @submit.prevent="submit">
        <input v-model="username" type="text" placeholder="用户名" />
        <input v-model="password" type="password" placeholder="密码" />
        <input v-model="phone" type="text" placeholder="手机（选填）" />
        <button type="submit" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
      </form>
      <p><router-link to="/login">已有账号？去登录</router-link></p>
    </div>
  </div>
</template>

<style scoped>
.page {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 0% 100%, rgba(56, 189, 248, 0.35), transparent 55%),
    radial-gradient(circle at 100% 0%, rgba(59, 130, 246, 0.25), transparent 50%),
    #f9fafb;
}

.box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  padding: 2.6rem 2.4rem 2.2rem;
  border-radius: 20px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  width: 360px;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.box h1 {
  margin: 0 0 0.4rem;
  font-size: 1.6rem;
  font-weight: 700;
  text-align: center;
  letter-spacing: 0.06em;
}

.subtitle {
  text-align: center;
  font-size: 0.86rem;
  color: #6b7280;
  margin-bottom: 1.4rem;
}

.box form {
  margin-top: 0.4rem;
}

.box input {
  display: block;
  width: 100%;
  padding: 0.6rem 0.7rem;
  margin-bottom: 0.8rem;
  border: 1px solid #bae6fd;
  border-radius: 10px;
  box-sizing: border-box;
  font-size: 0.95rem;
  background: #ffffff;
  color: #111827 !important;
}

.box input:focus {
  outline: none;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 1px #0ea5e9;
  background: #ffffff;
}

.box input::placeholder {
  color: #9ca3af;
  opacity: 1;
}

/* 去掉浏览器自动填充造成的灰色底/高亮 */
.box input:-webkit-autofill,
.box input:-webkit-autofill:hover,
.box input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0px 1000px #ffffff inset;
  box-shadow: 0 0 0px 1000px #ffffff inset;
  -webkit-text-fill-color: #111827 !important;
  caret-color: #111827;
}

.box button {
  width: 100%;
  padding: 0.7rem;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  color: #fff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  box-shadow: 0 14px 32px rgba(14, 165, 233, 0.45);
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.box button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 40px rgba(14, 165, 233, 0.55);
}

.box button:disabled {
  opacity: 0.75;
  cursor: not-allowed;
  box-shadow: none;
}

.box p {
  margin-top: 1.4rem;
  text-align: center;
  font-size: 0.9rem;
  color: #6b7280;
}

.box a {
  color: #0ea5e9;
  text-decoration: none;
}

.box a:hover {
  text-decoration: underline;
}
</style>
