#!/bin/bash

# =====================================================
# 生产环境部署脚本
# 用法: ./bin/deploy.sh <命令> [选项]
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
cd "$PROJECT_ROOT"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置
COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"

# 显示帮助
show_help() {
    echo -e "${CYAN}=============================================${NC}"
    echo -e "${CYAN}   生产环境部署工具${NC}"
    echo -e "${CYAN}=============================================${NC}"
    echo ""
    echo -e "${GREEN}用法:${NC} $0 <命令> [选项]"
    echo ""
    echo -e "${GREEN}部署命令:${NC}"
    echo "  up           - 启动所有服务（首次部署）"
    echo "  down         - 停止所有服务"
    echo "  restart      - 重启所有服务（不重建）"
    echo "  reload       - 重新加载配置（修改 .env 后使用）"
    echo ""
    echo -e "${GREEN}单模块更新:${NC}"
    echo "  frontend     - 更新前端模块"
    echo "  admin        - 更新管理端后端 (backend-api)"
    echo "  portal       - 更新门户端后端 (portal-api)"
    echo "  scheduler    - 更新调度模块"
    echo "  backend      - 更新所有后端模块 (admin + portal + scheduler)"
    echo "  all          - 更新所有模块"
    echo ""
    echo -e "${GREEN}运维命令:${NC}"
    echo "  status       - 查看服务状态"
    echo "  logs [服务]  - 查看日志 (可选: frontend/admin/portal/scheduler)"
    echo "  health       - 健康检查"
    echo "  pull         - 拉取最新代码 (git pull)"
    echo ""
    echo -e "${GREEN}选项:${NC}"
    echo "  --no-cache   - 不使用缓存重建镜像"
    echo "  --logs       - 更新后显示日志"
    echo "  -h, --help   - 显示帮助"
    echo ""
    echo -e "${GREEN}示例:${NC}"
    echo "  $0 up                       # 首次启动所有服务"
    echo "  $0 frontend                 # 只更新前端"
    echo "  $0 admin --logs             # 更新 admin 并查看日志"
    echo "  $0 reload                   # 修改 .env 后重新加载"
    echo "  $0 logs portal              # 查看 portal 日志"
    echo ""
}

# 检查环境
check_env() {
    if [ ! -f "$ENV_FILE" ]; then
        echo -e "${RED}错误: 未找到 .env 文件${NC}"
        echo -e "${YELLOW}请复制 .env.example 并配置:${NC}"
        echo "  cp .env.example .env"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: 未安装 Docker${NC}"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        echo -e "${RED}错误: Docker 未运行${NC}"
        exit 1
    fi
}

# 启动所有服务（首次部署）
deploy_up() {
    echo -e "\n${YELLOW}>>> 启动所有服务${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build
    echo -e "${GREEN}✓ 所有服务已启动${NC}"
}

# 停止所有服务
deploy_down() {
    echo -e "\n${YELLOW}>>> 停止所有服务${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" down
    echo -e "${GREEN}✓ 所有服务已停止${NC}"
}

# 重启所有服务（不重建）
deploy_restart() {
    echo -e "\n${YELLOW}>>> 重启所有服务${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" restart
    echo -e "${GREEN}✓ 所有服务已重启${NC}"
}

# 重新加载配置（修改 .env 后）
deploy_reload() {
    echo -e "\n${YELLOW}>>> 重新加载配置${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --force-recreate
    echo -e "${GREEN}✓ 配置已重新加载${NC}"
}

# 更新前端
deploy_frontend() {
    echo -e "\n${YELLOW}>>> 更新 Frontend 模块${NC}"
    
    echo -e "${BLUE}  -> 重建镜像${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $BUILD_OPTS frontend
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-deps frontend
    
    echo -e "${GREEN}✓ Frontend 更新完成${NC}"
}

# 更新 admin (backend-api)
deploy_admin() {
    echo -e "\n${YELLOW}>>> 更新 Admin 模块 (backend-api)${NC}"
    
    echo -e "${BLUE}  -> 重建镜像${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $BUILD_OPTS backend-api
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-deps backend-api
    
    echo -e "${GREEN}✓ Admin (backend-api) 更新完成${NC}"
}

# 更新 portal (portal-api)
deploy_portal() {
    echo -e "\n${YELLOW}>>> 更新 Portal 模块 (portal-api)${NC}"
    
    echo -e "${BLUE}  -> 重建镜像${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $BUILD_OPTS portal-api
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-deps portal-api
    
    echo -e "${GREEN}✓ Portal (portal-api) 更新完成${NC}"
}

# 更新 scheduler
deploy_scheduler() {
    echo -e "\n${YELLOW}>>> 更新 Scheduler 模块${NC}"
    
    # 检查是否有 scheduler 服务
    if ! grep -q "scheduler" "$COMPOSE_FILE" 2>/dev/null; then
        echo -e "${YELLOW}提示: docker-compose.yml 中未配置 scheduler 服务${NC}"
        echo -e "${YELLOW}Scheduler 当前集成在 backend-api 中运行${NC}"
        return 0
    fi
    
    echo -e "${BLUE}  -> 重建镜像${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build $BUILD_OPTS scheduler
    
    echo -e "${BLUE}  -> 重启容器${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-deps scheduler
    
    echo -e "${GREEN}✓ Scheduler 更新完成${NC}"
}

# 更新所有后端
deploy_backend() {
    deploy_admin
    deploy_portal
    deploy_scheduler
}

# 更新所有模块
deploy_all() {
    echo -e "\n${YELLOW}>>> 更新所有模块${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --build
    echo -e "${GREEN}✓ 所有模块更新完成${NC}"
}

# 显示状态
show_status() {
    echo -e "\n${CYAN}=============================================${NC}"
    echo -e "${CYAN}   服务状态${NC}"
    echo -e "${CYAN}=============================================${NC}"
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
}

# 显示日志
show_logs() {
    local service=$1
    echo -e "\n${CYAN}日志输出 (Ctrl+C 退出):${NC}"
    
    case $service in
        frontend)
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f frontend
            ;;
        admin|backend-api)
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f backend-api
            ;;
        portal|portal-api)
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f portal-api
            ;;
        scheduler)
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f scheduler 2>/dev/null || \
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f backend-api
            ;;
        "")
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f backend-api portal-api frontend
            ;;
        *)
            docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs -f "$service"
            ;;
    esac
}

# 健康检查
health_check() {
    echo -e "\n${CYAN}=============================================${NC}"
    echo -e "${CYAN}   健康检查${NC}"
    echo -e "${CYAN}=============================================${NC}"
    
    local all_healthy=true
    
    # 检查 Nginx
    echo -n "  Frontend (Nginx)    : "
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:81/health | grep -q "200"; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${RED}✗ 异常${NC}"
        all_healthy=false
    fi
    
    # 检查 Backend API
    echo -n "  Admin (Backend-API) : "
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null | grep -q "200"; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${YELLOW}? 无 actuator 或异常${NC}"
    fi
    
    # 检查 Portal API
    echo -n "  Portal (Portal-API) : "
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health 2>/dev/null | grep -q "200"; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${YELLOW}? 无 actuator 或异常${NC}"
    fi
    
    # 检查 PostgreSQL
    echo -n "  PostgreSQL          : "
    if docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres pg_isready -U postgres &>/dev/null; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${RED}✗ 异常${NC}"
        all_healthy=false
    fi
    
    # 检查 Redis
    echo -n "  Redis               : "
    if docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T redis redis-cli ping 2>/dev/null | grep -q "PONG"; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${RED}✗ 异常${NC}"
        all_healthy=false
    fi
    
    # 检查 WuKongIM
    echo -n "  WuKongIM            : "
    if curl -s http://localhost:5001/health 2>/dev/null | grep -q "ok\|healthy"; then
        echo -e "${GREEN}✓ 健康${NC}"
    else
        echo -e "${YELLOW}? 未知${NC}"
    fi
    
    echo ""
    if [ "$all_healthy" = true ]; then
        echo -e "${GREEN}所有核心服务运行正常${NC}"
    else
        echo -e "${RED}部分服务异常，请检查日志${NC}"
    fi
}

# 拉取最新代码
git_pull() {
    echo -e "\n${YELLOW}>>> 拉取最新代码${NC}"
    git pull
    echo -e "${GREEN}✓ 代码更新完成${NC}"
}

# 如果没有参数，显示帮助
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

COMMAND=$1
shift

# 参数解析
BUILD_OPTS=""
SHOW_LOGS=false

for arg in "$@"; do
    case $arg in
        --no-cache)
            BUILD_OPTS="--no-cache"
            ;;
        --logs)
            SHOW_LOGS=true
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
    esac
done

# 检查环境
check_env

# 主逻辑
START_TIME=$(date +%s)

echo -e "${CYAN}=============================================${NC}"
echo -e "${CYAN}   生产环境部署 - ${COMMAND}${NC}"
echo -e "${CYAN}=============================================${NC}"

case $COMMAND in
    up)
        deploy_up
        ;;
    down)
        deploy_down
        ;;
    restart)
        deploy_restart
        ;;
    reload)
        deploy_reload
        ;;
    frontend)
        deploy_frontend
        ;;
    admin)
        deploy_admin
        ;;
    portal)
        deploy_portal
        ;;
    scheduler)
        deploy_scheduler
        ;;
    backend)
        deploy_backend
        ;;
    all)
        deploy_all
        ;;
    status)
        show_status
        exit 0
        ;;
    logs)
        show_logs "$1"
        exit 0
        ;;
    health)
        health_check
        exit 0
        ;;
    pull)
        git_pull
        exit 0
        ;;
    -h|--help)
        show_help
        exit 0
        ;;
    *)
        echo -e "${RED}错误: 未知命令 '$COMMAND'${NC}"
        show_help
        exit 1
        ;;
esac

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo -e "\n${GREEN}=============================================${NC}"
echo -e "${GREEN}   完成! 耗时: ${ELAPSED}秒${NC}"
echo -e "${GREEN}=============================================${NC}"

show_status

if [ "$SHOW_LOGS" = true ]; then
    show_logs ""
fi
