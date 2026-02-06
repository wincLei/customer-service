#!/bin/sh
# Docker 容器启动脚本
# 用于在运行时注入环境变量到前端配置

CONFIG_FILE="/usr/share/nginx/html/config.js"

# 替换配置文件中的占位符
# 如果环境变量未设置，使用默认值
WUKONGIM_WS_URL="${VITE_WUKONGIM_WS_URL:-ws://localhost:5200}"

# 生成运行时配置文件
cat > "$CONFIG_FILE" << EOF
// 运行时配置（由 Docker 启动时动态注入）
// 生成时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
window.__RUNTIME_CONFIG__ = {
  WUKONGIM_WS_URL: "${WUKONGIM_WS_URL}"
};
EOF

echo "Runtime config generated:"
echo "  WUKONGIM_WS_URL: ${WUKONGIM_WS_URL}"

# 启动 nginx
exec nginx -g "daemon off;"
