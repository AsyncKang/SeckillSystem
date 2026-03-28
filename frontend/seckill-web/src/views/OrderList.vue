<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '../api'

const orders = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const { data } = await orderApi.myOrders()
    orders.value = data || []
  } catch (e) {
    alert(e.response?.data?.message || e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

const statusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消' }[s] ?? '未知')
</script>

<template>
  <div class="page">
    <nav class="nav">
      <router-link to="/">← 返回首页</router-link>
    </nav>
    <main class="main">
      <h1>我的订单</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <ul v-else class="list">
        <li v-for="o in orders" :key="o.id" class="item">
          <span class="name">{{ o.productName }}</span>
          <span class="price">¥{{ o.price }}</span>
          <span class="status">{{ statusText(o.status) }}</span>
          <span class="time">{{ o.createdAt }}</span>
        </li>
      </ul>
      <p v-if="!loading && orders.length === 0" class="empty">暂无订单</p>
    </main>
  </div>
</template>

<style scoped>
.page {
  width: 100vw;
  min-height: 100vh;
  background: #f3f5fb;
  color: #111827;
}

.nav {
  padding: 0.75rem 1.5rem;
  background: #151a30;
}

.nav a {
  color: #ffffff;
  text-decoration: none;
}

.main {
  max-width: 720px;
  margin: 0 auto;
  padding: 2.2rem 1.5rem 3rem;
}

.main h1 {
  margin-bottom: 1.2rem;
  font-size: 1.6rem;
  font-weight: 700;
}

.loading,
.empty {
  text-align: center;
  color: #6b7280;
  padding: 2rem;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.9rem 1.1rem;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.12);
  border: 1px solid #e2e8f0;
}

.item .name {
  flex: 1;
  font-weight: 500;
}

.item .price {
  color: #dc2626;
  font-weight: 600;
}

.item .status {
  color: #6b7280;
}

.item .time {
  font-size: 0.82rem;
  color: #9ca3af;
}
</style>
