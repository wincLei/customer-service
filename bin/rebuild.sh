#!/bin/bash

# =====================================================
# 统一模块构建和重启脚本
# 用法: ./bin/rebuild.sh <模块> [选项]
# 模块: admin, portal, frontend, backend, all
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
cd "$PROJECT_ROOT"

# 设置 Java 21 环境
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# 设置 Node.js v22.12.0 环境 (通过 nvm)
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm use v22.12.0 > /dev/null 2>&1 || true

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 显示帮助
show_help() {
    echo -e "${CYAN}=============================================${NC}"
    echo -e "${CYAN}   模块化构建和重启工具${NC}"
    echo -e "${CYAN}=============================================${NC}"
    echo ""
    echo -e "${GREEN}用法:${NC} $0 <模块> [选项]"
    echo ""
    echo -e "${GREEN}模块:${NC}"
    echo "  admin      - 构建并重启 Admin 后端模块"
    echo "  portal     - 构建并重启 Portal 后端模块"
    echo "  frontend   - 构建并重启前端模块"
    echo "  backend    - 构建并重启所有后端模块 (admin + portal)"
    echo "  all        - 构建并重启所有模块"
    echo "  infra      - 仅启动基础设施 (postgres, redis, rocketmq, wukongim)"
    echo "  status     - 查看所有容器状态"
    echo "  logs       - 查看所有应用容器日志"
    echo ""
    echo -e "${GREEN}选项:${NC}"
    echo "  --skip-build   跳过编译，仅重建镜像并重启容器"
    echo "  --logs         重启后持续显示日志"
    echo "  --force        强制重建 (--no-cache)"
    echo "  -h, --help     显示帮助信息"
    echo ""
    echo -e "${GREEN}示例:${NC}"
    echo "  $0 admin                    # 构建并重启 admin 模块"
    echo "  $0 admin --logs             # 构建重启后查看日志"
    echo "  $0 backend --skip-build     # 仅重建镜像重启后端"
    echo "  $0 all                      # 重建并重启所有模块"
    echo "  $0 status                   # 查看容器状态"
    echo ""
}

# 如果没有参数，显示帮助
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

MODULE=$1
shift

# 参数解析
SKIP_BUILD=false
SHOW_LOGS=false
FORCE_BUILD=false

for arg in "$@"; do
    case $arg in
        --skip-build)
            SKIP_BUILD=true
            ;;
        --logs)
            SHOW_LOGS=true
            ;;
        --force)
            FORCE_BUILD=true
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
    esac
done

# 构建选项
BUILD_OPTS=""
if [ "$FORCE_BUILD" = true ]; then
    BUILD_OPTS="--no-cache"
fi

# 构建 admin 模块
build_admin() {
    echo -e "\n${YELLOW}>>> 构建 Admin 模块${NC}"
    
    if [ "$SKIP_BUILD" = false ]; then
        echo -e "${BLUE}  -> Maven 编译${NC}"
        cd backend
        mvn -pl shared -am install -DskipTests -q
        mvn -pl admin-module -am package -DskipTests -q
        cd ..
    fi
    
    echo -e "${BLUE}  -> 构建 Docker 镜像${NC}"
    docker-compose -f docker-compose.dev.yml build $BUILD_OPTS backend-api
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker-compose -f docker-compose.dev.yml up -d backend-api
    
    echo -e "${GREEN}  ✓ Admin 模块更新完成${NC}"
}

# 构建 portal 模块
build_portal() {
    echo -e "\n${YELLOW}>>> 构建 Portal 模块${NC}"
    
    if [ "$SKIP_BUILD" = false ]; then
        echo -e "${BLUE}  -> Maven 编译${NC}"
        cd backend
        mvn -pl shared -am install -DskipTests -q
        mvn -pl portal-module -am package -DskipTests -q
        cd ..
    fi
    
    echo -e "${BLUE}  -> 构建 Docker 镜像${NC}"
    docker-compose -f docker-compose.dev.yml build $BUILD_OPTS portal-api
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker-compose -f docker-compose.dev.yml up -d portal-api
    
    echo -e "${GREEN}  ✓ Portal 模块更新完成${NC}"
}

# 构建 frontend 模块
build_frontend() {
    echo -e "\n${YELLOW}>>> 构建 Frontend 模块${NC}"
    
    if [ "$SKIP_BUILD" = false ]; then
        echo -e "${BLUE}  -> npm 构建${NC}"
        cd frontend
        if [ ! -d "node_modules" ]; then
            npm install --legacy-peer-deps
        fi
        npm run build
        cd ..
    fi
    
    echo -e "${BLUE}  -> 构建 Docker 镜像${NC}"
    docker-compose -f docker-compose.dev.yml build $BUILD_OPTS frontend
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker-compose -f docker-compose.dev.yml up -d frontend
    
    echo -e "${GREEN}  ✓ Frontend 模块更新完成${NC}"
}

# 启动基础设施
start_infra() {
    echo -e "\n${YELLOW}>>> 启动基础设施服务${NC}"
    docker-compose -f docker-compose.dev.yml up -d postgres redis rocketmq-namesrv rocketmq-broker wukongim
    echo -e "${GREEN}  ✓ 基础设施已启动${NC}"
}

# 显示状态
show_status() {
    echo -e "\n${CYAN}=============================================${NC}"
    echo -e "${CYAN}   容器状态${NC}"
    echo -e "${CYAN}=============================================${NC}"
    docker-compose -f docker-compose.dev.yml ps
}

# 显示日志
show_logs() {
    echo -e "\n${CYAN}容器日志 (Ctrl+C 退出):${NC}"
    docker-compose -f docker-compose.dev.yml logs -f backend-api portal-api frontend
}

# 主逻辑
START_TIME=$(date +%s)

echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   模块化构建工具 - ${MODULE}${NC}"
echo -e "${CYAN}=============================================${NC}"

case $MODULE in
    admin)
        build_admin
        ;;
    portal)
        build_portal
        ;;
    frontend)
        build_frontend
        ;;
    backend)
        build_admin
        build_portal
        ;;
    all)
        build_admin
        build_portal
        build_frontend
        ;;
    infra)
        start_infra
        ;;
    status)
        show_status
        exit 0
        ;;
    logs)
        show_logs
        exit 0
        ;;
    *)
        echo -e "${RED}错误: 未知模块 '$MODULE'${NC}"
        show_help
        exit 1
        ;;
esac

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${GREEN}=============================================${NC}"
echo -e "${GREEN}   完成! 总耗时: ${ELAPSED}秒${NC}"
echo -e "${GREEN}=============================================${NC}"

show_status

if [ "$SHOW_LOGS" = true ]; then
    show_logs
fi
