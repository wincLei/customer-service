FROM node:20 as build

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm install --only=production --no-fund --legacy-peer-deps --force

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段 - 使用nginx提供静态文件
FROM nginx:alpine

# 从构建阶段复制生成的文件
COPY --from=build /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY --from=build /app/nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]