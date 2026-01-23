#!/bin/bash

# =====================================================
# 数据库重置脚本
# 删除 PostgreSQL 数据卷并重新初始化
# 用法: ./bin/reset-db.sh
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
echo -e "${CYAN}   数据库重置脚本${NC}"
echo -e "${CYAN}=============================================${NC}"

# 确认操作
echo -e "${YELLOW}警告: 此操作将删除所有数据库数据并重新执行 init.sql${NC}"
read -p "确定要继续吗? (y/N): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}已取消操作${NC}"
    exit 0
fi

echo -e "\n${YELLOW}[1/4] 停止 PostgreSQL 容器...${NC}"
docker compose stop postgres 2>/dev/null || true

echo -e "${YELLOW}[2/4] 删除 PostgreSQL 容器...${NC}"
docker compose rm -f postgres 2>/dev/null || true

echo -e "${YELLOW}[3/4] 删除 PostgreSQL 数据卷...${NC}"
# 尝试删除数据卷（可能有不同的命名格式）
docker volume rm mini-customer-service_postgres_data 2>/dev/null || \
docker volume rm mini-customer-service-postgres_data 2>/dev/null || \
echo "数据卷可能已被删除或不存在"

echo -e "${YELLOW}[4/4] 重新启动 PostgreSQL 容器...${NC}"
docker compose up -d postgres

# 等待数据库就绪
echo -e "\n${CYAN}等待数据库初始化...${NC}"
for i in {1..30}; do
    if docker exec mini-customer-service-postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo -e "${GREEN}数据库已就绪!${NC}"
        break
    fi
    echo -n "."
    sleep 1
done

# 检查 init.sql 是否执行成功
echo -e "\n${CYAN}验证数据库初始化...${NC}"
TABLES=$(docker exec mini-customer-service-postgres psql -U postgres -d customer_service -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' ')

if [[ "$TABLES" -gt 0 ]]; then
    echo -e "${GREEN}✓ 数据库重置成功! 已创建 $TABLES 个表${NC}"
    
    # 显示一些关键数据
    echo -e "\n${CYAN}当前数据统计:${NC}"
    docker exec mini-customer-service-postgres psql -U postgres -d customer_service -c "
        SELECT '项目数' as 类型, COUNT(*)::text as 数量 FROM projects
        UNION ALL
        SELECT '用户数', COUNT(*)::text FROM sys_users
        UNION ALL
        SELECT '角色数', COUNT(*)::text FROM sys_roles
        UNION ALL
        SELECT '菜单数', COUNT(*)::text FROM sys_menus;
    " 2>/dev/null
else
    echo -e "${RED}✗ 数据库初始化可能失败，请检查日志${NC}"
    echo -e "${YELLOW}查看日志: docker logs mini-customer-service-postgres${NC}"
    exit 1
fi

echo -e "\n${GREEN}=============================================${NC}"
echo -e "${GREEN}   数据库重置完成!${NC}"
echo -e "${GREEN}=============================================${NC}"
