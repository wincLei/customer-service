# 前端构建阶段
FROM node:22-alpine AS builder

WORKDIR /build

# 复制 package.json
COPY frontend/package.json ./

# 安装依赖，使用 --legacy-peer-deps 处理可能的对等依赖冲突
RUN npm install --legacy-peer-deps

# 复制源代码
COPY frontend/src ./src
COPY frontend/vite.config.ts ./
COPY frontend/tsconfig.json ./
COPY frontend/tsconfig.node.json ./
COPY frontend/index.html ./
COPY frontend/postcss.config.js ./
COPY frontend/tailwind.config.ts ./

# 构建
RUN npm run build

# 运行阶段
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

# 从构建阶段复制构建产物
COPY --from=builder /build/dist .

# 复制 nginx 配置
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
