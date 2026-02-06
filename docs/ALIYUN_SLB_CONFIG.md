# 阿里云传统负载均衡（CLB）配置指南

本文档说明如何配置阿里云传统型负载均衡，通过**一个域名**访问所有服务。

## 架构说明

```
                          ┌─────────────────────────────────┐
                          │        阿里云 CLB (SLB)          │
                          │   your-domain.com (公网IP)       │
                          └─────────────────────────────────┘
                                         │
         ┌───────────────────────────────┼───────────────────────────────┐
         │                               │                               │
         ▼                               ▼                               ▼
   ┌───────────┐                  ┌───────────┐                  ┌───────────┐
   │   HTTP    │                  │  HTTPS    │                  │ WebSocket │
   │  端口 80  │                  │  端口 443 │                  │ 端口 5200 │
   │           │                  │           │                  │           │
   │ → 服务器  │                  │ → 服务器  │                  │ → 服务器  │
   │   :81     │                  │   :81     │                  │   :5200   │
   └───────────┘                  └───────────┘                  └───────────┘
         │                               │                               │
         ▼                               ▼                               ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                              ECS 服务器                                       │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │                         Nginx (容器端口:80, 映射:81)                     │  │
│  │                                                                        │  │
│  │  /               → 前端静态资源                                         │  │
│  │  /api/admin/*    → backend-api:8080                                   │  │
│  │  /api/portal/*   → portal-api:8081                                    │  │
│  │  /api/pub/*      → portal-api:8081                                    │  │
│  │  /ws/*           → wukongim:5200 (WebSocket 代理)                     │  │
│  │  /wkapi/*        → wukongim:5001 (HTTP API 代理)                      │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────────────────┐  │
│  │ WuKongIM    │  │ PostgreSQL  │  │ Redis                               │  │
│  │ :5200 (WS)  │  │ :5432       │  │ :6380 (映射)                        │  │
│  │ :5101 (TCP) │  │             │  │                                     │  │
│  │ :5001 (API) │  │             │  │                                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

## 一、阿里云 CLB 配置步骤

### 1. 创建负载均衡实例

1. 登录 [阿里云负载均衡控制台](https://slb.console.aliyun.com/)
2. 点击「创建负载均衡」
3. 选择配置：
   - **实例类型**：传统型负载均衡 CLB
   - **地域**：与 ECS 服务器相同地域
   - **网络类型**：公网
   - **计费方式**：按流量计费（推荐）
   - **实例规格**：根据业务量选择

### 2. 配置监听器

需要配置 **3 个监听器**：

#### 监听器 1：HTTP（端口 80）

| 配置项   | 值                 |
| -------- | ------------------ |
| 前端协议 | HTTP               |
| 前端端口 | 80                 |
| 后端协议 | HTTP               |
| 后端端口 | 81                 |
| 调度算法 | 轮询 (RR)          |
| 会话保持 | 开启（植入Cookie） |
| 健康检查 | 开启               |
| 检查端口 | 81                 |
| 检查路径 | /health            |

#### 监听器 2：HTTPS（端口 443）- 推荐

| 配置项   | 值                   |
| -------- | -------------------- |
| 前端协议 | HTTPS                |
| 前端端口 | 443                  |
| 后端协议 | HTTP                 |
| 后端端口 | 81                   |
| SSL 证书 | 选择已上传的域名证书 |
| 调度算法 | 轮询 (RR)            |
| 会话保持 | 开启（植入Cookie）   |
| 健康检查 | 开启                 |
| 检查端口 | 81                   |
| 检查路径 | /health              |

> **SSL 卸载**：CLB 负责 HTTPS 解密，后端使用 HTTP，减轻服务器压力。

#### 监听器 3：WebSocket（端口 5200）

| 配置项   | 值        |
| -------- | --------- |
| 前端协议 | TCP       |
| 前端端口 | 5200      |
| 后端协议 | TCP       |
| 后端端口 | 5200      |
| 调度算法 | 轮询 (RR) |
| 会话保持 | 关闭      |
| 健康检查 | 开启      |
| 检查端口 | 5200      |

> **注意**：WuKongIM WebSocket 需要 TCP 透传。

### 3. 添加后端服务器

1. 在负载均衡实例中选择「后端服务器」
2. 点击「添加后端服务器」
3. 选择您的 ECS 实例
4. 配置端口权重：
   - 端口 81（权重 100）
   - 端口 5200（权重 100）

### 4. 域名解析配置

1. 登录 [阿里云域名控制台](https://dns.console.aliyun.com/)
2. 添加 DNS 解析记录：

| 记录类型 | 主机记录   | 记录值      |
| -------- | ---------- | ----------- |
| A        | @ 或 www   | CLB 公网 IP |
| A        | cs（建议） | CLB 公网 IP |

例如：`cs.yourdomain.com` → CLB 公网 IP

## 二、端口使用说明

### 修改后的端口映射

| 服务         | 宿主机端口 | 容器端口 | 说明                   |
| ------------ | ---------- | -------- | ---------------------- |
| PostgreSQL   | 5432       | 5432     | 数据库（内网）         |
| Redis        | **6380**   | 6379     | 缓存（内网）           |
| WuKongIM API | 5001       | 5001     | HTTP API（内网）       |
| WuKongIM TCP | **5101**   | 5100     | TCP 连接（内网）       |
| WuKongIM WS  | 5200       | 5200     | WebSocket（**公网**）  |
| Backend API  | 8080       | 8080     | 管理端 API（内网）     |
| Portal API   | 8082       | 8081     | 用户端 API（内网）     |
| Frontend     | **81**     | 80       | Nginx 前端（**公网**） |

### 需要对公网开放的端口

| 端口 | 用途                    |
| ---- | ----------------------- |
| 81   | Nginx 前端和 API 代理   |
| 5200 | WuKongIM WebSocket 通信 |

### 仅内网访问的端口

| 端口 | 用途              |
| ---- | ----------------- |
| 5432 | PostgreSQL 数据库 |
| 6380 | Redis 缓存        |
| 5001 | WuKongIM HTTP API |
| 5101 | WuKongIM TCP      |
| 8080 | Backend API       |
| 8082 | Portal API        |

## 三、前端配置修改

### WebSocket 连接地址配置

前端需要配置 WuKongIM WebSocket 连接地址。修改前端配置文件：

**方式一：使用 CLB 的 WebSocket 端口（推荐）**

```javascript
// 前端 .env 或配置文件
VITE_WUKONGIM_WS_URL=wss://cs.yourdomain.com:5200
// 或者 HTTP 环境
VITE_WUKONGIM_WS_URL=ws://cs.yourdomain.com:5200
```

**方式二：使用 Nginx 代理的 WebSocket（需要 SSL）**

Nginx 已配置 `/ws/` 路径代理到 WuKongIM WebSocket：

```javascript
// 使用 Nginx 代理路径
VITE_WUKONGIM_WS_URL=wss://cs.yourdomain.com/ws
```

### API 地址配置

API 通过 Nginx 反向代理，无需单独配置：

```javascript
// 前端自动使用相对路径，通过 Nginx 代理
// /api/admin/*  → 管理端 API
// /api/portal/* → 用户端 API
// /api/pub/*    → 公开 API
```

## 四、服务器安全组配置

在阿里云 ECS 安全组中配置入方向规则：

| 端口范围  | 授权对象       | 说明                    |
| --------- | -------------- | ----------------------- |
| 81/81     | CLB 私网 IP 段 | 允许 CLB 访问 Nginx     |
| 5200/5200 | CLB 私网 IP 段 | 允许 CLB 访问 WebSocket |
| 22/22     | 您的 IP        | SSH 管理（可选）        |

> **安全建议**：仅允许 CLB 的私网 IP 段访问后端端口，不要直接开放给公网。

### CLB 私网 IP 段查询

在负载均衡控制台查看实例详情，获取 CLB 使用的私网 IP 段。通常为 VPC 内的地址段。

## 五、健康检查配置

### Nginx 健康检查端点

Nginx 已配置 `/health` 端点：

```nginx
location /health {
    access_log off;
    return 200 "healthy\n";
    add_header Content-Type text/plain;
}
```

### CLB 健康检查配置

| 配置项   | 值      |
| -------- | ------- |
| 检查协议 | HTTP    |
| 检查端口 | 81      |
| 检查路径 | /health |
| 正常阈值 | 3       |
| 异常阈值 | 3       |
| 检查间隔 | 5秒     |

## 六、HTTPS 证书配置（推荐）

### 1. 申请 SSL 证书

1. 登录 [阿里云 SSL 证书控制台](https://yundun.console.aliyun.com/?p=cas)
2. 购买或申请免费证书
3. 完成域名验证

### 2. 上传证书到 CLB

1. 在 CLB 控制台选择「证书管理」
2. 点击「创建证书」
3. 选择「从阿里云云盾导入」或「上传第三方证书」

### 3. 配置 HTTPS 监听器

创建监听器时选择 HTTPS 协议，并绑定已上传的证书。

## 七、常见访问路径

配置完成后，用户可通过以下路径访问：

| 路径                                 | 用途           |
| ------------------------------------ | -------------- |
| `https://cs.yourdomain.com/`         | 前端首页       |
| `https://cs.yourdomain.com/#/portal` | 用户端门户     |
| `https://cs.yourdomain.com/#/admin`  | 管理后台       |
| `https://cs.yourdomain.com/#/h5`     | H5 用户端      |
| `wss://cs.yourdomain.com:5200`       | WebSocket 连接 |

## 八、验证配置

### 1. 检查服务状态

```bash
# 检查容器运行状态
docker compose ps

# 检查端口监听
netstat -tlnp | grep -E '81|5200|6380|5101'
```

### 2. 测试健康检查

```bash
# 本地测试
curl http://localhost:81/health

# 通过 CLB 测试
curl http://cs.yourdomain.com/health
```

### 3. 测试 WebSocket 连接

```bash
# 使用 wscat 测试（需安装 npm install -g wscat）
wscat -c ws://cs.yourdomain.com:5200
```

### 4. 测试 API 访问

```bash
# 测试管理端 API
curl https://cs.yourdomain.com/api/admin/health

# 测试门户端 API
curl https://cs.yourdomain.com/api/portal/health
```

## 九、故障排查

### CLB 健康检查失败

1. 检查安全组是否允许 CLB 访问
2. 检查后端服务是否正常运行
3. 检查健康检查路径是否正确

### WebSocket 连接失败

1. 确认 5200 端口在安全组中开放
2. 检查 WuKongIM 服务是否正常
3. 前端 WebSocket URL 是否正确

### 502 Bad Gateway

1. 检查后端服务是否启动
2. 查看 Nginx 错误日志：
   ```bash
   docker logs mini-customer-service-frontend
   ```

## 十、总结

通过以上配置，您可以实现：

- ✅ 单域名访问所有服务
- ✅ HTTPS 加密传输
- ✅ WebSocket 实时通信
- ✅ 负载均衡和高可用
- ✅ SSL 卸载减轻服务器压力
