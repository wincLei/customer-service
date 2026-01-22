#!/bin/bash

echo "🧪 开始测试登录功能..."
echo ""

# 定义颜色
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 确保使用 Node v22.12.0
echo -e "${BLUE}📌 确保使用 Node v22.12.0${NC}"
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm use v22.12.0
echo -e "${GREEN}✅ 当前 Node 版本: $(node --version)${NC}"
echo ""

# 1. 检查当前目录
echo -e "${BLUE}📂 当前工作目录:${NC}"
pwd
echo ""

# 2. 检查 Docker 服务状态
echo -e "${BLUE}🐳 检查 Docker 服务状态:${NC}"
docker-compose ps
echo ""

# 3. 检查后端服务是否可访问
echo -e "${BLUE}🔍 测试后端 API 连接:${NC}"
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null | grep -q "200"; then
    echo -e "${GREEN}✅ 后端服务运行正常${NC}"
else
    echo -e "${YELLOW}⚠️  后端服务可能未启动，尝试访问 /api/admin/auth/captcha${NC}"
    curl -i http://localhost:8080/api/admin/auth/captcha 2>/dev/null || echo -e "${RED}❌ 后端服务不可访问${NC}"
fi
echo ""

# 4. 检查前端依赖
echo -e "${BLUE}📦 检查前端依赖:${NC}"
cd frontend
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}⚠️  未找到 node_modules，正在安装依赖...${NC}"
    npm install
else
    echo -e "${GREEN}✅ 依赖已安装${NC}"
fi
echo ""

# 5. 启动前端开发服务器（后台运行）
echo -e "${BLUE}🚀 启动前端开发服务器:${NC}"
echo -e "${YELLOW}正在启动 Vite 开发服务器...${NC}"
echo ""
echo -e "${GREEN}✨ 测试步骤：${NC}"
echo -e "  1. 等待服务器启动（约10秒）"
echo -e "  2. 打开浏览器访问: ${BLUE}http://localhost:5173${NC}"
echo -e "  3. 使用测试账号登录:"
echo -e "     - 账号: admin"
echo -e "     - 密码: admin123"
echo -e "     - 验证码: 随意输入4位（开发模式）"
echo -e "  4. 观察是否成功跳转到 ${BLUE}/admin/dashboard${NC}"
echo ""
echo -e "${YELLOW}💡 提示：${NC}"
echo -e "  - 如果登录成功但未跳转，请检查浏览器控制台"
echo -e "  - 如果看到 404 错误，检查后端服务是否正常"
echo -e "  - 按 Ctrl+C 停止开发服务器"
echo ""

# 启动开发服务器
npm run dev
