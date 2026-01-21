# 前端构建阶段
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package.json 和 package-lock.json
COPY package.json package-lock.json ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY src ./src
COPY vite.config.ts ./
COPY tsconfig.json ./
COPY index.html ./

# 构建
RUN npm run build

# 运行阶段
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

# 从构建阶段复制构建产物
COPY --from=builder /build/dist .

# 复制 nginx 配置
COPY ../nginx/default.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
