#!/bin/bash

# =====================================================
# Frontend 模块快速构建和重启脚本
# 用法: ./bin/rebuild-frontend.sh [--skip-build] [--logs]
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
cd "$PROJECT_ROOT"

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
NC='\033[0m' # No Color

# 参数解析
SKIP_BUILD=false
SHOW_LOGS=false

for arg in "$@"; do
    case $arg in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --logs)
            SHOW_LOGS=true
            shift
            ;;
        -h|--help)
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --skip-build   跳过 npm 构建，仅重建 Docker 镜像并重启容器"
            echo "  --logs         重启后显示容器日志"
            echo "  -h, --help     显示帮助信息"
            exit 0
            ;;
    esac
done

echo -e "${BLUE}=============================================${NC}"
echo -e "${BLUE}   Frontend 模块快速构建和重启${NC}"
echo -e "${BLUE}=============================================${NC}"

START_TIME=$(date +%s)

# Step 1: npm 构建
if [ "$SKIP_BUILD" = false ]; then
    echo -e "\n${YELLOW}[1/3] 正在构建 Frontend...${NC}"
    cd frontend
    
    # 安装依赖（如果 node_modules 不存在）
    if [ ! -d "node_modules" ]; then
        echo -e "${BLUE}  -> 安装依赖${NC}"
        npm install --legacy-peer-deps
    fi
    
    # 构建
    echo -e "${BLUE}  -> 执行构建${NC}"
    npm run build
    
    cd ..
    echo -e "${GREEN}  ✓ Frontend 构建完成${NC}"
else
    echo -e "\n${YELLOW}[1/3] 跳过 Frontend 构建${NC}"
fi

# Step 2: 重建 Docker 镜像
echo -e "\n${YELLOW}[2/3] 重建 Docker 镜像...${NC}"
docker-compose -f docker-compose.dev.yml build --no-cache frontend
echo -e "${GREEN}  ✓ Docker 镜像构建完成${NC}"

# Step 3: 重启容器
echo -e "\n${YELLOW}[3/3] 重启容器...${NC}"
docker-compose -f docker-compose.dev.yml up -d frontend
echo -e "${GREEN}  ✓ 容器重启完成${NC}"

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${GREEN}=============================================${NC}"
echo -e "${GREEN}   Frontend 模块更新完成! 耗时: ${ELAPSED}秒${NC}"
echo -e "${GREEN}=============================================${NC}"

# 显示容器状态
echo -e "\n${BLUE}容器状态:${NC}"
docker-compose -f docker-compose.dev.yml ps frontend

# 显示日志
if [ "$SHOW_LOGS" = true ]; then
    echo -e "\n${BLUE}容器日志 (Ctrl+C 退出):${NC}"
    docker-compose -f docker-compose.dev.yml logs -f frontend
fi
