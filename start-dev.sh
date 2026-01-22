#!/bin/bash

# 加载 nvm
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

# 确保使用 v22.12.0
nvm use v22.12.0

echo "✅ 当前 Node 版本: $(node --version)"
echo ""

# 进入 frontend 目录
cd "$(dirname "$0")/frontend"

# 检查 8080 端口是否被占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 8080 已被占用，停止旧进程..."
    lsof -Pi :8080 -sTCP:LISTEN -t | xargs kill -9
    sleep 2
fi

# 检查 5173 端口是否被占用
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 5173 已被占用，停止旧进程..."
    lsof -Pi :5173 -sTCP:LISTEN -t | xargs kill -9
    sleep 2
fi

echo "🚀 启动 Mock 后端服务器 (端口 8080)..."
node mock-server.js &
MOCK_PID=$!
echo "Mock 服务器 PID: $MOCK_PID"

# 等待 Mock 服务器启动
sleep 2

echo ""
echo "🚀 启动 Vite 开发服务器 (端口 5173)..."
npm run dev &
VITE_PID=$!
echo "Vite 服务器 PID: $VITE_PID"

echo ""
echo "========================================="
echo "✅ 服务启动完成！"
echo "========================================="
echo ""
echo "📍 前端地址: http://localhost:5173"
echo "📍 Mock API: http://localhost:8080"
echo ""
echo "🔑 测试账号:"
echo "   账号: admin"
echo "   密码: admin123"
echo "   验证码: 任意4位数字"
echo ""
echo "⏹️  停止服务: kill $MOCK_PID $VITE_PID"
echo "或按 Ctrl+C"
echo ""

# 保持脚本运行
wait
