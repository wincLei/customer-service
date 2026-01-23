#!/bin/bash

# =====================================================
# 停止所有开发环境容器
# 用法: ./bin/stop-dev.sh [--volumes]
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
cd "$PROJECT_ROOT"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   停止开发环境${NC}"
echo -e "${CYAN}=============================================${NC}"

# 参数解析
REMOVE_VOLUMES=false

for arg in "$@"; do
    case $arg in
        --volumes|-v)
            REMOVE_VOLUMES=true
            ;;
        -h|--help)
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --volumes, -v   同时删除数据卷 (会清除数据库数据)"
            echo "  -h, --help      显示帮助信息"
            exit 0
            ;;
    esac
done

if [ "$REMOVE_VOLUMES" = true ]; then
    echo -e "${YELLOW}警告: 将删除所有数据卷，包括数据库数据!${NC}"
    read -p "确定要继续吗? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}已取消${NC}"
        exit 1
    fi
    docker-compose -f docker-compose.dev.yml down -v
    echo -e "${GREEN}✓ 所有容器和数据卷已删除${NC}"
else
    docker-compose -f docker-compose.dev.yml down
    echo -e "${GREEN}✓ 所有容器已停止 (数据卷已保留)${NC}"
fi

echo -e "\n${CYAN}要重新启动开发环境，请运行:${NC}"
echo -e "  ./bin/init-dev.sh    # 全量初始化"
echo -e "  或"
echo -e "  docker-compose -f docker-compose.dev.yml up -d  # 快速启动"
