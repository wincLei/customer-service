# 前端运行镜像 (使用本地预编译的 dist)
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

# 从本地复制预编译的构建产物
COPY dist .

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
