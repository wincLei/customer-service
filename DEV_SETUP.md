# 开发环境设置指南

## 🖥️ 开发环境要求

### 最小配置

- CPU: 2核
- 内存: 4GB RAM
- 磁盘: 20GB 可用空间
- 操作系统: macOS / Linux / Windows (WSL2)

### 推荐配置

- CPU: 4核+
- 内存: 8GB+ RAM
- 磁盘: 50GB+ SSD
- 操作系统: macOS Big Sur+ 或 Linux (Ubuntu 20.04+)

## 📦 必需工具安装

### 1. Docker & Docker Compose

**macOS:**

```bash
# 使用 Homebrew
brew install docker
brew install docker-compose

# 或直接下载 Docker Desktop
# https://www.docker.com/products/docker-desktop
```

**Linux (Ubuntu/Debian):**

```bash
# 安装 Docker
sudo apt-get update
sudo apt-get install docker.io docker-compose

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker
```

**验证:**

```bash
docker --version
docker-compose --version
```

### 2. Node.js v22.12.0

**macOS:**

```bash
brew install node@22

# 或使用 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install v22.12.0
nvm use v22.12.0
```

**Linux:**

```bash
curl -sL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs
```

**验证:**

```bash
node --version  # v22.12.0
npm --version   # 10.x.x+
```

### 3. Java 21+

**macOS:**

```bash
brew install openjdk@21

# 配置 JAVA_HOME
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Linux:**

```bash
sudo apt-get install openjdk-21-jdk

# 验证
java -version
```

### 4. Maven 3.9+

**macOS:**

```bash
brew install maven

# 或从官网下载
# https://maven.apache.org/download.cgi
```

**Linux:**

```bash
sudo apt-get install maven
```

**验证:**

```bash
mvn --version
```

## 🔧 IDE 设置

### VS Code（推荐用于前端）

**必需插件:**

- Volar (Vue Language Support)
- TypeScript Vue Plugin
- Tailwind CSS IntelliSense
- ESLint
- Prettier

**安装:**

```bash
code --install-extension Vue.volar
code --install-extension Vue.vscode-typescript-vue-plugin
code --install-extension bradlc.vscode-tailwindcss
code --install-extension dbaeumer.vscode-eslint
code --install-extension esbenp.prettier-vscode
```

**settings.json 配置:**

```json
{
  "[vue]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.formatOnSave": true
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "vetur.validation.template": true
}
```

### IntelliJ IDEA（推荐用于后端）

**必需插件:**

- Spring Boot (内置)
- Spring Assistant (内置)
- Maven Helper
- Lombok
- Database

**配置:**

1. 打开 Preferences > Build, Execution, Deployment > Compiler > Annotation Processors
2. 启用 "Enable annotation processing"
3. 添加 Lombok 处理器

## 📝 项目配置

### 克隆项目

```bash
cd /Users/leijiang/Documents/vs-workspace
# 项目已在此位置: mini-customer-service
cd mini-customer-service
```

### 后端配置

**1. 配置数据库连接 (application.yml)**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/customer_service
    username: postgres
    password: postgres123
```

**2. 初始化数据库**

```bash
# 启动 PostgreSQL
docker-compose up -d postgres

# 等待数据库就绪
docker-compose exec postgres pg_isready

# 执行初始化脚本
docker-compose exec postgres psql -U postgres -d customer_service -f /docker-entrypoint-initdb.d/init.sql
```

**3. 启动后端服务**

```bash
cd backend

# 编译
mvn clean install

# 启动管理模块
mvn spring-boot:run -pl admin-module

# 或启动用户模块（新终端）
mvn spring-boot:run -pl portal-module
```

### 前端配置

**1. 安装依赖**

```bash
cd frontend

npm install
# 或使用 yarn
yarn install
```

**2. 启动开发服务器**

```bash
npm run dev
```

访问: http://localhost:5173

**3. 配置 API 代理**

修改 `vite.config.ts`:

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

## 🗄️ 数据库工具

### pgAdmin（Web界面）

```bash
# 启动 pgAdmin
docker run -d \
  --name pgadmin \
  -p 5050:80 \
  -e PGADMIN_DEFAULT_EMAIL=admin@example.com \
  -e PGADMIN_DEFAULT_PASSWORD=admin \
  dpage/pgadmin4
```

访问: http://localhost:5050

### CLI工具

```bash
# 使用 psql
psql -h localhost -U postgres -d customer_service

# 常用命令
\dt                 # 列出所有表
\d+ 表名            # 查看表结构
SELECT * FROM agents; # 查询数据
```

## 📊 其他工具

### Redis 管理工具

```bash
# 安装 redis-cli
brew install redis

# 连接 Redis
redis-cli -h localhost -p 6379

# 常用命令
KEYS *              # 列出所有 key
GET key_name        # 获取值
DEL key_name        # 删除 key
FLUSHALL            # 清空所有数据
```

### Postman（API测试）

```bash
# 安装
brew install --cask postman

# 或访问 https://www.postman.com/downloads/
```

导入项目中的 API 集合：

- 创建新的 Workspace
- 导入 `postman_collection.json` (如果有)
- 配置环境变量

## 🚀 首次启动流程

```bash
# 1. 启动所有依赖服务
docker-compose up -d postgres redis wukongim rocketmq-namesrv rocketmq-broker

# 2. 等待服务就绪（检查健康状态）
docker-compose exec postgres pg_isready
docker-compose exec redis redis-cli ping

# 3. 执行数据库初始化
psql -h localhost -U postgres -d customer_service -f sql/init.sql

# 4. 启动后端
cd backend && mvn spring-boot:run -pl admin-module

# 5. 新终端启动前端
cd frontend && npm run dev

# 6. 访问应用
# 前端: http://localhost:5173
# API: http://localhost:8080
```

## 🔍 常用开发命令

### 后端

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 查看依赖树
mvn dependency:tree

# 更新依赖
mvn versions:display-dependency-updates
```

### 前端

```bash
# 开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查
npm run lint

# 代码格式化
npx prettier --write src/

# 性能分析
npm run build -- --report
```

## 📚 快速链接

| 工具       | URL                              | 用户名            | 密码        |
| ---------- | -------------------------------- | ----------------- | ----------- |
| 应用       | http://localhost:5173            | -                 | -           |
| API文档    | http://localhost:8080/swagger-ui | -                 | -           |
| pgAdmin    | http://localhost:5050            | admin@example.com | admin       |
| Redis      | localhost:6379                   | -                 | -           |
| PostgreSQL | localhost:5432                   | postgres          | postgres123 |

## 🐛 故障排查

### Maven 构建失败

```bash
# 清除本地 Maven 缓存
rm -rf ~/.m2/repository

# 重新构建
mvn clean install
```

### Node 模块问题

```bash
# 清除 node_modules 和锁文件
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### Docker 容器无法启动

```bash
# 检查磁盘空间
docker system df

# 清理未使用的镜像
docker system prune -a

# 重新构建镜像
docker-compose build --no-cache
```

## 📖 推荐学习资源

### 后端

- Spring Boot官方文档: https://spring.io/projects/spring-boot
- JPA文档: https://spring.io/projects/spring-data-jpa
- PostgreSQL文档: https://www.postgresql.org/docs/

### 前端

- Vue 3官方文档: https://vuejs.org/
- Vite文档: https://vitejs.dev/
- Element Plus文档: https://element-plus.org/

### DevOps

- Docker官方文档: https://docs.docker.com/
- Nginx文档: https://nginx.org/en/docs/

---

**更新时间**: 2026年1月20日
