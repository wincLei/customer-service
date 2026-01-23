#!/bin/bash

# =====================================================
# 开发环境初始化脚本
# 首次启动时使用，完成全量构建
# 用法: ./bin/init-dev.sh
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
cd "$PROJECT_ROOT"

# 设置 Java 21 环境
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
if [ -z "$JAVA_HOME" ]; then
    echo "错误: 未找到 Java 21，请先安装 Java 21"
    exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

# 设置 Node.js v22.12.0 环境 (通过 nvm)
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm use v22.12.0 > /dev/null 2>&1 || {
    echo "错误: 未找到 Node.js v22.12.0，请运行: nvm install v22.12.0"
    exit 1
}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   开发环境初始化${NC}"
echo -e "${CYAN}=============================================${NC}"
echo -e "${BLUE}  Java: $(java -version 2>&1 | head -1)${NC}"
echo -e "${BLUE}  Node: $(node --version)${NC}"

START_TIME=$(date +%s)

# Step 1: 构建后端
echo -e "\n${YELLOW}[1/4] 构建后端模块...${NC}"
cd backend

echo -e "${BLUE}  -> 安装 shared 模块${NC}"
mvn -pl shared install -DskipTests -q

echo -e "${BLUE}  -> 构建 admin-module${NC}"
mvn -pl admin-module -am package -DskipTests -q

echo -e "${BLUE}  -> 构建 portal-module${NC}"
mvn -pl portal-module -am package -DskipTests -q

cd ..
echo -e "${GREEN}  ✓ 后端构建完成${NC}"

# Step 2: 构建前端
echo -e "\n${YELLOW}[2/4] 构建前端...${NC}"
cd frontend

if [ ! -d "node_modules" ]; then
    echo -e "${BLUE}  -> 安装依赖${NC}"
    npm install --legacy-peer-deps
fi

echo -e "${BLUE}  -> 执行构建${NC}"
npm run build

cd ..
echo -e "${GREEN}  ✓ 前端构建完成${NC}"

# Step 3: 启动基础设施
echo -e "\n${YELLOW}[3/4] 启动基础设施服务...${NC}"
docker-compose -f docker-compose.dev.yml up -d postgres redis rocketmq-namesrv rocketmq-broker wukongim

echo -e "${BLUE}  -> 等待数据库就绪...${NC}"
sleep 10

echo -e "${GREEN}  ✓ 基础设施已启动${NC}"

# Step 4: 构建并启动应用
echo -e "\n${YELLOW}[4/4] 构建并启动应用服务...${NC}"
docker-compose -f docker-compose.dev.yml build backend-api portal-api frontend
docker-compose -f docker-compose.dev.yml up -d backend-api portal-api frontend

echo -e "${GREEN}  ✓ 应用服务已启动${NC}"

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${GREEN}=============================================${NC}"
echo -e "${GREEN}   初始化完成! 总耗时: ${ELAPSED}秒${NC}"
echo -e "${GREEN}=============================================${NC}"

echo -e "\n${CYAN}服务访问地址:${NC}"
echo -e "  前端:      http://localhost"
echo -e "  Admin API: http://localhost:8080"
echo -e "  Portal API: http://localhost:8082"
echo ""

echo -e "${CYAN}容器状态:${NC}"
docker-compose -f docker-compose.dev.yml ps

echo -e "\n${YELLOW}后续更新模块请使用:${NC}"
echo -e "  ./bin/rebuild.sh admin      # 更新 admin 模块"
echo -e "  ./bin/rebuild.sh portal     # 更新 portal 模块"
echo -e "  ./bin/rebuild.sh frontend   # 更新前端"
echo -e "  ./bin/rebuild.sh backend    # 更新所有后端"
echo -e "  ./bin/rebuild.sh all        # 更新所有模块"
echo -e "  ./bin/rebuild.sh status     # 查看状态"
echo -e "  ./bin/rebuild.sh logs       # 查看日志"
