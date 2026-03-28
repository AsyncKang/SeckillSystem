<script setup>
import { ref, onMounted } from 'vue'
import { productAdminApi } from '../api'

const tab = ref('activity')
const activities = ref([])
const products = ref([])

const productForm = ref({ name: '', imageUrl: '', originalPrice: '', seckillPrice: '' })
const activityForm = ref({ productId: '', totalStock: '', startTime: '', endTime: '' })

onMounted(() => { loadActivities() })

async function loadActivities() {
  try {
    const { data } = await productAdminApi.listActivities()
    activities.value = data || []
  } catch (e) {
    alert(e.response?.data?.message || '加载失败')
  }
}

async function createProduct() {
  const p = productForm.value
  if (!p.name || !p.originalPrice || !p.seckillPrice) { alert('请填写名称、原价、秒杀价'); return }
  try {
    await productAdminApi.createProduct(p)
    alert('商品创建成功')
    productForm.value = { name: '', imageUrl: '', originalPrice: '', seckillPrice: '' }
    loadActivities()
  } catch (e) {
    alert(e.response?.data?.message || '创建失败')
  }
}

async function createActivity() {
  const a = activityForm.value
  if (!a.productId || !a.totalStock || !a.startTime || !a.endTime) { alert('请填写完整'); return }
  try {
    await productAdminApi.createActivity({
      productId: Number(a.productId),
      totalStock: Number(a.totalStock),
      startTime: new Date(a.startTime).toISOString(),
      endTime: new Date(a.endTime).toISOString()
    })
    alert('活动创建成功，请点击「上线」并预热库存')
    activityForm.value = { productId: '', totalStock: '', startTime: '', endTime: '' }
    loadActivities()
  } catch (e) {
    alert(e.response?.data?.message || '创建失败')
  }
}

async function setOnline(id) {
  try {
    await productAdminApi.setOnline(id)
    alert('已上线并预热库存')
    loadActivities()
  } catch (e) {
    alert(e.response?.data?.message || '操作失败')
  }
}

function formatTime(iso) {
  return iso ? new Date(iso).toLocaleString('zh-CN') : ''
}
</script>

<template>
  <div class="page">
    <nav class="nav">
      <router-link to="/">← 返回首页</router-link>
    </nav>
    <main class="main">
      <h1>管理后台</h1>
      <div class="tabs">
        <button :class="{ active: tab === 'activity' }" @click="tab = 'activity'">活动列表</button>
        <button :class="{ active: tab === 'product' }" @click="tab = 'product'">发布商品</button>
        <button :class="{ active: tab === 'newActivity' }" @click="tab = 'newActivity'">创建活动</button>
      </div>

      <div v-show="tab === 'activity'" class="panel">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>商品ID</th><th>库存</th><th>开始</th><th>结束</th><th>上线</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="a in activities" :key="a.id">
              <td>{{ a.id }}</td>
              <td>{{ a.productId }}</td>
              <td>{{ a.totalStock }}</td>
              <td>{{ formatTime(a.startTime) }}</td>
              <td>{{ formatTime(a.endTime) }}</td>
              <td>{{ a.online ? '是' : '否' }}</td>
              <td>
                <button v-if="!a.online" class="btn-sm" @click="setOnline(a.id)">上线</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-show="tab === 'product'" class="panel">
        <form @submit.prevent="createProduct" class="form">
          <input v-model="productForm.name" placeholder="商品名称" required />
          <input v-model="productForm.imageUrl" placeholder="图片URL（选填）" />
          <input v-model="productForm.originalPrice" placeholder="原价" type="number" step="0.01" required />
          <input v-model="productForm.seckillPrice" placeholder="秒杀价" type="number" step="0.01" required />
          <button type="submit">创建商品</button>
        </form>
      </div>

      <div v-show="tab === 'newActivity'" class="panel">
        <form @submit.prevent="createActivity" class="form">
          <input v-model="activityForm.productId" placeholder="商品ID" type="number" required />
          <input v-model="activityForm.totalStock" placeholder="秒杀库存" type="number" required />
          <input v-model="activityForm.startTime" placeholder="开始时间" type="datetime-local" required />
          <input v-model="activityForm.endTime" placeholder="结束时间" type="datetime-local" required />
          <button type="submit">创建活动</button>
        </form>
        <p class="hint">创建后请在「活动列表」中点击「上线」以预热库存并对外展示。</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
:root {
  color-scheme: light;
}

.page {
  width: 100vw;
  min-height: 100vh;
  background: #f3f5fb;
  color: #1f2933;
}

.nav {
  padding: 0.75rem 1.5rem;
  background: #151a30;
}

.nav a {
  color: #fff;
  text-decoration: none;
  font-weight: 500;
}

.main {
  max-width: 960px;
  margin: 0 auto;
  padding: 2rem 1.5rem 3rem;
}

.main h1 {
  margin-bottom: 1.5rem;
  font-size: 1.8rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #151a30;
}

.tabs {
  margin-bottom: 1rem;
}

.tabs button {
  margin-right: 0.75rem;
  padding: 0.45rem 1.1rem;
  border-radius: 999px;
  border: 1px solid #d0d7ec;
  background: #ffffff;
  color: #334155;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.15s ease;
}

.tabs button:hover {
  background: #eef1ff;
  border-color: #7b91ff;
}

.tabs button.active {
  background: #4f46e5;
  color: #fff;
  border-color: #4f46e5;
  box-shadow: 0 6px 18px rgba(79, 70, 229, 0.35);
}

.panel {
  background: #ffffff;
  padding: 1.25rem 1.5rem;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  border: 1px solid #e2e8f0;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.table thead th {
  background: #5f6caf;
  color: #ffffff;
  padding: 0.6rem 0.5rem;
  font-weight: 600;
  text-align: left;
}

.table th,
.table td {
  border-bottom: 1px solid #e2e8f0;
}

.table tbody tr:nth-child(odd) td {
  background: #f6f7ff;
}

.table tbody tr:nth-child(even) td {
  background: #edf0ff;
}

.table tbody tr:hover td {
  background: #dde3ff;
}

.table td {
  padding: 0.55rem 0.5rem;
  color: #1f2933;
}

.btn-sm {
  padding: 0.25rem 0.7rem;
  font-size: 0.8rem;
  background: #16a34a;
  color: #fff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.15s ease, transform 0.15s ease;
}

.btn-sm:hover {
  background: #15803d;
  transform: translateY(-1px);
}

.form {
  max-width: 480px;
  margin: 0 auto;
}

.form input {
  display: block;
  width: 100%;
  max-width: 420px;
  padding: 0.55rem 0.6rem;
  margin: 0 auto 0.8rem auto;
  border: 1px solid #d0d7ec;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 0.9rem;
  background: #f9fafb;
  color: #111827;
}

.form input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 1px #4f46e5;
  background: #ffffff;
}

.form input::placeholder {
  color: #6b7280;
  opacity: 1;
}

.form button {
  padding: 0.55rem 1.3rem;
  background: #4f46e5;
  color: #fff;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.35);
  transition: background 0.15s ease, transform 0.15s ease;
}

.form button:hover {
  background: #4338ca;
  transform: translateY(-1px);
}

.hint {
  margin-top: 0.75rem;
  font-size: 0.85rem;
  color: #6b7280;
}
</style>
