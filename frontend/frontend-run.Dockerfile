# 前端运行镜像 (使用本地预编译的 dist)
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

# 从本地复制预编译的构建产物
COPY dist .

# 复制启动脚本（支持运行时配置注入）
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# 暴露端口
EXPOSE 80

# 使用启动脚本（运行时将环境变量注入到 config.js）
ENTRYPOINT ["/docker-entrypoint.sh"]
