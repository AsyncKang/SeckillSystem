<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '../api'

const router = useRouter()
const activities = ref([])
const loading = ref(true)
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

onMounted(async () => {
  try {
    const { data } = await productApi.listActivities()
    activities.value = data || []
  } finally {
    loading.value = false
  }
})

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleString('zh-CN')
}

function countdown(endTime) {
  const end = new Date(endTime).getTime()
  const now = Date.now()
  if (now >= end) return '已结束'
  const t = Math.floor((end - now) / 1000)
  const h = Math.floor(t / 3600)
  const m = Math.floor((t % 3600) / 60)
  const s = t % 60
  return `${h}时${m}分${s}秒`
}

const countdowns = ref({})
let timer = null
onMounted(() => {
  timer = setInterval(() => {
    activities.value.forEach(a => {
      countdowns.value[a.activityId] = countdown(a.endTime)
    })
  }, 1000)
})
import { onUnmounted } from 'vue'
onUnmounted(() => clearInterval(timer))

function canSeckill(a) {
  const now = new Date()
  const start = new Date(a.startTime)
  const end = new Date(a.endTime)
  return now >= start && now < end && a.stockRemain > 0
}

async function doSeckill(activityId) {
  if (!user.value) {
    router.push('/login')
    return
  }
  try {
    const { data } = await productApi.seckill(activityId)
    alert(data?.message || '抢购成功')
    const { data: list } = await productApi.listActivities()
    activities.value = list || []
  } catch (e) {
    alert(e.response?.data?.message || e.message || '抢购失败')
    const { data: list } = await productApi.listActivities()
    activities.value = list || []
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  location.reload()
}

function activityStatus(a) {
  const now = new Date()
  const start = new Date(a.startTime)
  const end = new Date(a.endTime)
  if (now < start) return '未开始'
  if (now >= end) return '已结束'
  if (a.stockRemain <= 0) return '已抢光'
  return '不可抢购'
}
</script>

<template>
  <div class="home">
    <nav class="nav">
      <span class="logo">秒杀系统</span>
      <div class="links">
        <router-link to="/">首页</router-link>
        <router-link v-if="user" to="/orders">我的订单</router-link>
        <router-link v-if="user?.admin" to="/admin">管理后台</router-link>
        <template v-if="user">
          <span class="user">{{ user.username }}</span>
          <a href="#" @click.prevent="logout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </nav>

    <main class="main">
      <h1>秒杀活动</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else class="activity-list">
        <div v-for="a in activities" :key="a.activityId" class="card">
          <img v-if="a.imageUrl" :src="a.imageUrl" class="thumb" alt="" />
          <div v-else class="thumb placeholder">图</div>
          <div class="info">
            <h3>{{ a.productName }}</h3>
            <p class="price">
              <span class="original">¥{{ a.originalPrice }}</span>
              <span class="seckill">¥{{ a.seckillPrice }}</span>
            </p>
            <p class="time">开始 {{ formatTime(a.startTime) }} · 结束 {{ formatTime(a.endTime) }}</p>
            <p class="stock">剩余 {{ a.stockRemain }} 件</p>
            <p class="countdown">倒计时: {{ countdowns[a.activityId] ?? countdown(a.endTime) }}</p>
            <button
              class="btn-seckill"
              :disabled="!canSeckill(a)"
              @click="doSeckill(a.activityId)"
            >
              {{ canSeckill(a) ? '立即抢购' : activityStatus(a) }}
            </button>
          </div>
        </div>
      </div>
      <p v-if="!loading && activities.length === 0" class="empty">暂无秒杀活动</p>
    </main>
  </div>
</template>

<style scoped>
.home {
  width: 100vw;
  min-height: 100vh;
  background: #f3f5fb;
  color: #111827;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1.8rem;
  background: #151a30;
  color: #e5e7eb;
}

.logo {
  font-size: 1.3rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.links {
  display: flex;
  gap: 1.2rem;
  align-items: center;
  font-size: 0.9rem;
}

.links a,
.links .user {
  color: #cbd5f5;
  text-decoration: none;
}

.links a:hover {
  color: #ffffff;
}

.main {
  max-width: 960px;
  margin: 0 auto;
  padding: 2.2rem 1.5rem 3rem;
}

.main h1 {
  margin-bottom: 1.2rem;
  font-size: 1.8rem;
  font-weight: 700;
}

.loading,
.empty {
  text-align: center;
  color: #6b7280;
  padding: 2rem;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
}

.card {
  display: flex;
  gap: 1.3rem;
  padding: 1.1rem 1.3rem;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 14px 35px rgba(15, 23, 42, 0.12);
  border: 1px solid #e2e8f0;
}

.thumb {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 10px;
}

.thumb.placeholder {
  background: linear-gradient(135deg, #e0e7ff, #f3e8ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  font-size: 0.9rem;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info h3 {
  margin: 0 0 0.4rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.price {
  margin-bottom: 0.4rem;
}

.price .original {
  text-decoration: line-through;
  color: #9ca3af;
  margin-right: 0.45rem;
}

.price .seckill {
  color: #dc2626;
  font-weight: 700;
}

.time,
.stock,
.countdown {
  margin: 0.18rem 0;
  font-size: 0.88rem;
  color: #6b7280;
}

.btn-seckill {
  align-self: flex-start;
  margin-top: 0.65rem;
  padding: 0.55rem 1.3rem;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #ffffff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.4);
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.btn-seckill:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 32px rgba(79, 70, 229, 0.5);
}

.btn-seckill:disabled {
  background: #d1d5db;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.8;
}
</style>
