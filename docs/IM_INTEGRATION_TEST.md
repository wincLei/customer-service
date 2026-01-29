# WuKongIM 集成测试方案

## 🎯 测试目标

验证 WuKongIM 即时通讯功能在 Docker 环境下的完整工作流程。

## 📋 前置条件

### 1. 确保所有服务运行正常

```bash
./bin/rebuild.sh status
```

预期看到以下容器运行中：

- `mini-customer-service-backend` - Admin API
- `mini-customer-service-portal-api` - Portal API
- `mini-customer-service-frontend` - 前端
- `wukongim` - IM 服务
- `mini-customer-service-postgres` - 数据库
- `mini-customer-service-redis` - 缓存

### 2. 验证 WuKongIM 服务

```bash
# 检查 WuKongIM 日志
docker logs wukongim 2>&1 | head -30

# 应该看到:
# Token Auth: ✅ enabled
# 🚀 Server is ready!

# 测试 API 连通性
curl -s http://localhost:5001/user/token \
  -X POST \
  -H "Content-Type: application/json" \
  -H "token: wukongim_admin_secret_2026" \
  -d '{"uid":"test","token":"test123","device_flag":1,"device_level":0}'

# 预期返回: {"status":200}
```

---

## 🧪 测试场景

### 场景 1: 客服工作台 IM 连接

**步骤:**

1. 打开浏览器访问: http://localhost/admin/login
2. 使用测试账号登录: `agent1` / `agent123`
3. 进入「工作台」页面
4. 检查左上角 IM 状态指示器

**预期结果:**

- 🟢 显示绿色「已连接」状态
- 浏览器控制台显示:
  ```
  [IM] initIMConnection called, agentId: X imInstance: false
  [IM] Fetching token for uid: agent_X deviceFlag: 1
  [IM] Token API response status: 200
  [IM] Agent IM Token obtained: xxxxxxxx...
  [IM] Connecting to WebSocket: ws://localhost:5200
  Agent IM connected successfully
  ```

**验证后端日志:**

```bash
docker logs mini-customer-service-backend 2>&1 | grep -i "token\|IM" | tail -10
```

---

### 场景 2: H5 用户端 IM 连接

**步骤:**

1. 打开浏览器访问: http://localhost/h5/chat
2. 系统自动创建访客身份
3. 检查页面顶部 IM 状态

**预期结果:**

- 🟢 显示「已连接」状态
- 可以发送消息

**验证 Portal 日志:**

```bash
docker logs mini-customer-service-portal-api 2>&1 | grep -i "token\|IM" | tail -10
```

---

### 场景 3: 实时消息发送与接收

**准备工作:**

- 窗口 A: 客服工作台 (http://localhost/admin/workbench)
- 窗口 B: H5 用户端 (http://localhost/h5/chat)

**测试步骤:**

1. **用户发起会话:**
   - 在窗口 B (H5端) 发送消息: "你好，我需要帮助"
2. **客服接入:**
   - 在窗口 A (工作台) 看到新会话出现在「排队中」
   - 点击会话进行接入

3. **双向通信:**
   - 客服回复: "您好，请问有什么可以帮您？"
   - 验证用户端是否实时收到
   - 用户再次发送消息
   - 验证客服端是否实时收到

**预期结果:**

- ✅ 消息双向实时传递，无需刷新页面
- ✅ 消息时间戳正确
- ✅ 发送方/接收方显示正确

---

### 场景 4: 连接断开与重连

**测试步骤:**

1. 重启 WuKongIM 服务:

   ```bash
   docker restart wukongim
   ```

2. 观察客服工作台:
   - IM 状态应变为 🔴「未连接」或 🟡「连接中」
3. 等待 10-30 秒:
   - SDK 应自动重连
   - 状态恢复为 🟢「已连接」

**预期结果:**

- ✅ 断线后自动重连
- ✅ 重连后消息收发正常

---

### 场景 5: Token 验证安全性

**测试步骤:**

1. 测试无 token 调用 API:
   ```bash
   curl -v http://localhost:5001/user/token \
     -X POST \
     -H "Content-Type: application/json" \
     -d '{"uid":"hacker","token":"fake","device_flag":1,"device_level":0}'
   ```

**预期结果:**

- 返回 `401 Unauthorized`
- 未授权的请求被拒绝

---

## 📊 健康检查脚本

创建一个快速健康检查脚本:

```bash
#!/bin/bash
# 文件: bin/check-im-health.sh

echo "=== WuKongIM 健康检查 ==="

# 1. 检查容器状态
echo -n "1. WuKongIM 容器: "
if docker ps | grep -q wukongim; then
    echo "✅ 运行中"
else
    echo "❌ 未运行"
    exit 1
fi

# 2. 检查 API 端口
echo -n "2. API 端口 (5001): "
if curl -s http://localhost:5001/ > /dev/null 2>&1 || curl -s -o /dev/null -w "%{http_code}" http://localhost:5001/user/token -X POST -H "Content-Type: application/json" -H "token: wukongim_admin_secret_2026" -d '{}' 2>&1 | grep -q "200\|400"; then
    echo "✅ 可访问"
else
    echo "❌ 不可访问"
fi

# 3. 检查 WebSocket 端口
echo -n "3. WebSocket 端口 (5200): "
if nc -z localhost 5200 2>/dev/null; then
    echo "✅ 可访问"
else
    echo "❌ 不可访问"
fi

# 4. 测试 Token 注册
echo -n "4. Token 注册: "
RESULT=$(curl -s http://localhost:5001/user/token \
  -X POST \
  -H "Content-Type: application/json" \
  -H "token: wukongim_admin_secret_2026" \
  -d '{"uid":"health_check","token":"test123","device_flag":1,"device_level":0}')

if echo "$RESULT" | grep -q '"status":200'; then
    echo "✅ 成功"
else
    echo "❌ 失败: $RESULT"
fi

# 5. 检查后端连接
echo -n "5. 后端 IM 配置: "
ENV_URL=$(docker exec mini-customer-service-backend env | grep WUKONGIM_API_URL | cut -d= -f2)
ENV_KEY=$(docker exec mini-customer-service-backend env | grep WUKONGIM_APP_KEY | cut -d= -f2)

if [ "$ENV_URL" = "http://wukongim:5001" ] && [ -n "$ENV_KEY" ]; then
    echo "✅ 正确"
else
    echo "❌ 配置错误 (URL: $ENV_URL)"
fi

echo ""
echo "=== 检查完成 ==="
```

---

## 🔧 常见问题排查

### 问题 1: IM 状态一直显示「连接中」

**检查步骤:**

```bash
# 查看 WuKongIM 日志
docker logs wukongim 2>&1 | tail -20

# 查看后端日志
docker logs mini-customer-service-backend 2>&1 | grep -i "token\|error" | tail -20
```

**可能原因:**

- Token 注册失败 (检查 APP_KEY 配置)
- deviceFlag 不匹配 (前后端必须一致)
- 网络问题

### 问题 2: 消息发送成功但对方收不到

**检查步骤:**

```bash
# 检查 WebSocket 连接
docker logs wukongim 2>&1 | grep -i "connect\|message" | tail -20
```

**可能原因:**

- 频道未创建
- 订阅者未添加
- 消息格式错误

### 问题 3: 连接后立即断开

**检查步骤:**

```bash
# 检查认证错误
docker logs wukongim 2>&1 | grep -i "token err\|auth" | tail -10
```

**可能原因:**

- Token 已过期或无效
- deviceFlag 与注册时不一致

---

## 📈 性能指标

在生产环境中，建议监控以下指标:

| 指标               | 正常范围 | 告警阈值 |
| ------------------ | -------- | -------- |
| WebSocket 连接数   | -        | > 10000  |
| 消息延迟           | < 100ms  | > 500ms  |
| Token API 响应时间 | < 50ms   | > 200ms  |
| 内存使用           | < 512MB  | > 1GB    |

---

## ✅ 测试检查清单

- [ ] WuKongIM 容器正常运行
- [ ] Token 验证 API 返回 200
- [ ] 客服工作台 IM 连接成功
- [ ] H5 用户端 IM 连接成功
- [ ] 消息实时双向传递
- [ ] 断线自动重连
- [ ] 无 token 请求被拒绝 (401)
- [ ] 后端日志无错误

---

## 🚀 快速测试命令

```bash
# 一键健康检查
./bin/check-im-health.sh

# 查看所有 IM 相关日志
docker logs wukongim 2>&1 | tail -50
docker logs mini-customer-service-backend 2>&1 | grep -i "wukong\|token\|IM" | tail -20

# 重启 IM 相关服务
docker-compose -f docker-compose.dev.yml restart wukongim
./bin/rebuild.sh backend
```
