# 极简客服系统 - 生产环境部署指南

本文档详细说明如何将极简客服系统部署到生产环境。

## 目录

- [系统要求](#系统要求)
- [部署架构](#部署架构)
- [部署前准备](#部署前准备)
- [快速部署](#快速部署)
- [详细配置](#详细配置)
- [安全加固](#安全加固)
- [运维管理](#运维管理)
- [常见问题](#常见问题)

---

## 系统要求

### 硬件配置

| 配置项 | 最低配置 | 推荐配置 |
| ------ | -------- | -------- |
| CPU    | 2 核     | 4 核+    |
| 内存   | 4 GB     | 8 GB+    |
| 磁盘   | 40 GB    | 100 GB+  |
| 带宽   | 5 Mbps   | 10 Mbps+ |

### 软件要求

- **操作系统**: Ubuntu 20.04/22.04 LTS, CentOS 7/8, Debian 10/11
- **Docker**: 20.10+
- **Docker Compose**: v2.0+

### 端口要求

| 端口 | 服务         | 说明                                     |
| ---- | ------------ | ---------------------------------------- |
| 80   | Nginx        | HTTP 入口（建议改为 443 HTTPS）          |
| 5432 | PostgreSQL   | 数据库（**生产环境建议只允许内网访问**） |
| 6379 | Redis        | 缓存（**生产环境建议只允许内网访问**）   |
| 5001 | WuKongIM API | IM HTTP 接口（**建议只允许内网访问**）   |
| 5100 | WuKongIM TCP | IM TCP 连接（**建议只允许内网访问**）    |
| 5200 | WuKongIM WS  | WebSocket 连接（前端需要访问）           |

---

## 部署架构

```
                    ┌──────────────┐
                    │   用户访问    │
                    │  (HTTP/HTTPS)│
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │    Nginx     │ :80/:443
                    │   (前端+反代) │
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
  ┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐
  │ Backend API │   │ Portal API  │   │  WuKongIM   │
  │   :8080     │   │   :8081     │   │ :5001/5200  │
  └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
         │                 │                 │
         └────────┬────────┴────────┬────────┘
                  │                 │
           ┌──────▼──────┐   ┌──────▼──────┐
           │ PostgreSQL  │   │    Redis    │
           │   :5432     │   │   :6379     │
           └─────────────┘   └─────────────┘
```

---

## 部署前准备

### 1. 准备服务器

```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl git

# CentOS
sudo yum update -y
sudo yum install -y curl git
```

### 2. 安装 Docker

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | sh

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户加入 docker 组（需重新登录生效）
sudo usermod -aG docker $USER

# 验证安装
docker --version
docker compose version
```

### 3. 克隆项目

```bash
# 克隆代码
git clone https://github.com/your-repo/mini-customer-service.git
cd mini-customer-service
```

### 4. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件
vim .env
```

**重要配置项说明：**

```bash
# ========== 数据库配置（生产环境请修改密码！）==========
POSTGRES_USER=postgres
POSTGRES_PASSWORD=YOUR_STRONG_PASSWORD_HERE  # ⚠️ 必须修改为强密码
POSTGRES_DB=customer_service

# ========== WuKongIM 配置 ==========
WUKONGIM_APP_KEY=YOUR_WUKONGIM_SECRET_KEY   # ⚠️ 必须修改为随机密钥

# ========== 阿里云 OSS 配置（用于图片上传）==========
ALIYUN_OSS_ACCESS_KEY_ID=your_access_key_id
ALIYUN_OSS_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_OSS_BUCKET=your_bucket_name
ALIYUN_OSS_ENDPOINT=oss-cn-shanghai.aliyuncs.com
ALIYUN_OSS_REGION=cn-shanghai
ALIYUN_OSS_DOMAIN=https://your-bucket.oss-cn-shanghai.aliyuncs.com  # 或自定义域名
```

### 5. 生成安全密钥

```bash
# 生成数据库密码
openssl rand -base64 32

# 生成 WuKongIM 密钥
openssl rand -hex 32

# 生成用户密码哈希（用于初始管理员账号）
./bin/generate-password-hash.sh your_admin_password
```

---

## 快速部署

### 一键部署

```bash
# 构建并启动所有服务
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 验证部署

```bash
# 检查各服务健康状态
docker compose ps

# 预期输出（所有服务应为 running/healthy）：
# NAME                             STATUS
# mini-customer-service-postgres   healthy
# mini-customer-service-redis      healthy
# wukongim                         running
# mini-customer-service-backend    running
# mini-customer-service-portal-api running
# mini-customer-service-frontend   running
```

### 访问系统

| 入口      | 地址                               | 说明            |
| --------- | ---------------------------------- | --------------- |
| 管理后台  | http://your-domain/admin           | 管理员/客服登录 |
| 用户端 H5 | http://your-domain/h5?project_id=1 | 用户聊天界面    |
| 健康检查  | http://your-domain/health          | 服务状态检查    |

### 默认账号

| 用户名 | 密码     | 角色   |
| ------ | -------- | ------ |
| admin  | admin123 | 管理员 |
| agent1 | admin123 | 客服   |
| agent2 | admin123 | 客服   |

> ⚠️ **首次登录后请立即修改默认密码！**

---

## 详细配置

### 数据库配置

数据库会在首次启动时自动初始化（通过 `sql/init.sql`）。

如需手动连接数据库：

```bash
# 进入 PostgreSQL 容器
docker exec -it mini-customer-service-postgres psql -U postgres -d customer_service

# 常用 SQL
\dt                     # 查看所有表
\d+ table_name          # 查看表结构
SELECT * FROM sys_users; # 查看用户
```

### 修改管理员密码

```bash
# 1. 生成新密码哈希
./bin/generate-password-hash.sh new_password

# 2. 更新数据库
docker exec -it mini-customer-service-postgres psql -U postgres -d customer_service -c \
  "UPDATE sys_users SET password_hash = '\$2b\$12\$...' WHERE username = 'admin';"
```

### 配置 HTTPS（推荐）

1. 准备 SSL 证书（可使用 Let's Encrypt 免费证书）

2. 修改 `nginx/default.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;

    # ... 其余配置保持不变 ...
}
```

3. 挂载证书到容器：

```yaml
# docker-compose.yml 中的 frontend 服务
frontend:
  volumes:
    - ./nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    - ./ssl:/etc/nginx/ssl:ro # 添加这行
  ports:
    - "80:80"
    - "443:443" # 添加这行
```

### 配置域名

1. 将域名解析到服务器 IP

2. 修改 `nginx/default.conf` 中的 `server_name`：

```nginx
server_name your-domain.com;
```

3. 重启 Nginx：

```bash
docker compose restart frontend
```

---

## 安全加固

### 1. 修改默认密码

```bash
# 修改数据库密码后更新 .env
POSTGRES_PASSWORD=new_strong_password

# 重建服务
docker compose down
docker compose up -d
```

### 2. 限制端口暴露

修改 `docker-compose.yml`，将内部服务端口仅绑定到 localhost：

```yaml
postgres:
  ports:
    - "127.0.0.1:5432:5432" # 仅本地访问

redis:
  ports:
    - "127.0.0.1:6379:6379" # 仅本地访问

wukongim:
  ports:
    - "127.0.0.1:5001:5001" # API 仅本地访问
    - "127.0.0.1:5100:5100" # TCP 仅本地访问
    - "5200:5200" # WebSocket 保持对外（前端需要）
```

### 3. 配置防火墙

```bash
# Ubuntu (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 5200/tcp  # WuKongIM WebSocket
sudo ufw enable

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=5200/tcp
sudo firewall-cmd --reload
```

### 4. 定期备份数据库

```bash
# 创建备份脚本 backup.sh
#!/bin/bash
BACKUP_DIR="/data/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份 PostgreSQL
docker exec mini-customer-service-postgres pg_dump -U postgres customer_service | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 保留最近 7 天备份
find $BACKUP_DIR -type f -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/db_$DATE.sql.gz"
```

```bash
# 添加定时任务（每天凌晨 2 点备份）
chmod +x backup.sh
crontab -e
# 添加：0 2 * * * /path/to/backup.sh
```

---

## 运维管理

### 常用命令

```bash
# 查看所有服务状态
docker compose ps

# 查看日志
docker compose logs -f                    # 所有服务
docker compose logs -f backend-api        # 特定服务
docker compose logs --tail=100 backend-api # 最近100行

# 重启服务
docker compose restart                    # 重启所有
docker compose restart backend-api        # 重启特定服务

# 停止服务
docker compose stop

# 完全停止并删除容器
docker compose down

# 重新构建并启动
docker compose up -d --build

# 进入容器
docker exec -it mini-customer-service-backend sh
docker exec -it mini-customer-service-postgres psql -U postgres
```

### 更新部署

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 备份数据库
./backup.sh

# 3. 重新构建并启动
docker compose down
docker compose up -d --build

# 4. 验证服务
docker compose ps
curl http://localhost/health
```

### 查看资源使用

```bash
# 查看容器资源占用
docker stats

# 查看磁盘使用
docker system df

# 清理未使用的镜像和容器
docker system prune -a
```

### 日志管理

```bash
# 配置 Docker 日志大小限制（/etc/docker/daemon.json）
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}

# 重启 Docker 使配置生效
sudo systemctl restart docker
```

---

## 常见问题

### Q1: 服务启动失败

```bash
# 查看详细日志
docker compose logs backend-api

# 常见原因：
# 1. 数据库未就绪 - 等待 postgres healthy 后再启动
# 2. 端口被占用 - 检查端口占用：netstat -tlnp | grep 8080
# 3. 内存不足 - 检查内存：free -h
```

### Q2: 无法连接数据库

```bash
# 检查数据库状态
docker compose ps postgres

# 检查数据库日志
docker compose logs postgres

# 测试连接
docker exec mini-customer-service-postgres pg_isready
```

### Q3: WebSocket 连接失败

```bash
# 检查 WuKongIM 状态
docker compose logs wukongim

# 确认 5200 端口可访问
curl -I http://localhost:5200

# 检查 Nginx WebSocket 配置
# 确保有 proxy_http_version 1.1 和 Upgrade 头
```

### Q4: 图片上传失败

```bash
# 检查 OSS 配置
docker compose exec backend-api env | grep OSS

# 确认 OSS 权限
# 1. Bucket 需要设置为公共读
# 2. 跨域配置允许前端域名
```

### Q5: 如何重置数据库

```bash
# ⚠️ 警告：这会删除所有数据！
docker compose down -v  # -v 会删除数据卷
docker compose up -d    # 重新初始化
```

---

## 技术支持

- **GitHub Issues**: [提交问题](https://github.com/your-repo/mini-customer-service/issues)
- **文档**: 查看项目 README.md 和 docs/ 目录

---

## 附录：服务清单

| 服务        | 镜像                 | 端口           | 说明                 |
| ----------- | -------------------- | -------------- | -------------------- |
| postgres    | postgres:15-alpine   | 5432           | PostgreSQL 数据库    |
| redis       | redis:7-alpine       | 6379           | Redis 缓存           |
| wukongim    | wukongim/wukongim:v2 | 5001/5100/5200 | 即时通讯服务         |
| backend-api | 自构建               | 8080           | 管理端 API           |
| portal-api  | 自构建               | 8081           | 用户端 API           |
| frontend    | 自构建               | 80             | Nginx + 前端静态资源 |

---

_文档更新日期: 2026-02-05_
