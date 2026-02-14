FROM node:20 as build

WORKDIR /app

COPY package*.json ./

# 强制安装，忽略已存在文件
RUN npm install --only=production --no-fund --legacy-peer-deps --force

COPY . .
RUN npm run build

# 只复制已构建的静态文件
FROM nginx:alpine

# 复制本地构建的dist文件（需要先在本地构建）
COPY dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
