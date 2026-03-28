import http from 'k6/http'
import { check, sleep } from 'k6'
import { Counter } from 'k6/metrics'

/**
 * Docker 方式运行（macOS）：
 * cd backend/k6
 * docker run --rm -i -v "$PWD":/scripts -w /scripts grafana/k6 run seckill.js
 *
 * 可用环境变量：
 * - BASE_URL      默认 http://host.docker.internal:8080   （容器访问宿主机）
 * - ACTIVITY_ID   默认 1
 * - USERS         默认 200（并发用户数/注册用户数）
 * - DURATION      默认 10s
 * - SLEEP_MS      默认 100（每次请求后 sleep 毫秒）
 * - RUN_ID        默认 Date.now()（避免用户名冲突）
 */

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080'
const ACTIVITY_ID = Number(__ENV.ACTIVITY_ID || '1')
const USERS = Number(__ENV.USERS || '60')
const DURATION = __ENV.DURATION || '10s'
const SLEEP_MS = Number(__ENV.SLEEP_MS || '100')
const RUN_ID = __ENV.RUN_ID || String(Date.now())

export const options = {
  vus: USERS,
  duration: DURATION,
  thresholds: {
    http_req_duration: ['p(95)<800'],
  },
}

const status200 = new Counter('status_200')
const status409 = new Counter('status_409')
const status429 = new Counter('status_429')
const status401 = new Counter('status_401')
const status403 = new Counter('status_403')
const status400 = new Counter('status_400')
const statusOther = new Counter('status_other')

function registerOrLogin(username, password) {
  const registerRes = http.post(`${BASE_URL}/api/user/register`, JSON.stringify({
    username,
    password,
    phone: '',
  }), { headers: { 'Content-Type': 'application/json' } })

  // 注册成功
  if (registerRes.status === 200) {
    const body = registerRes.json()
    return body?.token
  }

  // 用户已存在等情况，走登录
  const loginRes = http.post(`${BASE_URL}/api/user/login`, JSON.stringify({
    username,
    password,
  }), { headers: { 'Content-Type': 'application/json' } })

  if (loginRes.status !== 200) {
    return null
  }
  const body = loginRes.json()
  return body?.token
}

export function setup() {
  const tokens = []
  const password = 'pass123456'

  // 压测前先拉一次活动列表，方便你确认活动是否“已上线/已预热”
  const before = http.get(`${BASE_URL}/api/product/activities`)
  console.log(`[k6] activities(before) status=${before.status} body=${before.body?.slice?.(0, 300)}`)

  for (let i = 1; i <= USERS; i++) {
    const username = `k6_${RUN_ID}_${i}`
    const token = registerOrLogin(username, password)
    if (token) tokens.push(token)
  }

  if (tokens.length === 0) {
    throw new Error('未获取到任何 token，请确认后端网关/用户服务已启动且 BASE_URL 可访问')
  }

  return { tokens }
}

export default function (data) {
  const tokens = data.tokens
  const token = tokens[(__VU - 1) % tokens.length]

  const res = http.post(
    `${BASE_URL}/api/product/seckill/${ACTIVITY_ID}`,
    null,
    { headers: { Authorization: `Bearer ${token}` } }
  )

  // 200=成功；409=库存不足/重复抢购；429=网关限流；401/403=鉴权失败；400=活动未上线/未开始/已结束
  if (res.status === 200) status200.add(1)
  else if (res.status === 409) status409.add(1)
  else if (res.status === 429) status429.add(1)
  else if (res.status === 401) status401.add(1)
  else if (res.status === 403) status403.add(1)
  else if (res.status === 400) status400.add(1)
  else statusOther.add(1)

  check(res, {
    'status is expected': (r) => [200, 409, 429, 401, 403, 400].includes(r.status),
  })

  sleep(SLEEP_MS / 1000)
}

export function teardown() {
  const after = http.get(`${BASE_URL}/api/product/activities`)
  console.log(`[k6] activities(after) status=${after.status} body=${after.body?.slice?.(0, 300)}`)
}

