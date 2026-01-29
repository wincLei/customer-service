#!/bin/bash
# WuKongIM 健康检查脚本

set -e

echo "=== WuKongIM 健康检查 ==="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

success() { echo -e "${GREEN}✅ $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠️  $1${NC}"; }

ERRORS=0

# 1. 检查容器状态
echo -n "1. WuKongIM 容器状态: "
if docker ps --format '{{.Names}}' | grep -q "^wukongim$"; then
    success "运行中"
else
    error "未运行"
    ERRORS=$((ERRORS + 1))
fi

# 2. 检查 API 端口
echo -n "2. API 端口 (5001): "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5001/user/token \
  -X POST \
  -H "Content-Type: application/json" \
  -H "token: wukongim_admin_secret_2026" \
  -d '{"uid":"check","token":"check","device_flag":1,"device_level":0}' 2>/dev/null || echo "000")

if [ "$HTTP_CODE" = "200" ]; then
    success "可访问"
else
    error "不可访问 (HTTP: $HTTP_CODE)"
    ERRORS=$((ERRORS + 1))
fi

# 3. 检查 WebSocket 端口
echo -n "3. WebSocket 端口 (5200): "
if nc -z localhost 5200 2>/dev/null; then
    success "可访问"
else
    error "不可访问"
    ERRORS=$((ERRORS + 1))
fi

# 4. 检查 Token 验证是否开启
echo -n "4. Token 验证: "
NO_TOKEN_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5001/user/token \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"uid":"unauthorized","token":"test","device_flag":1,"device_level":0}' 2>/dev/null || echo "000")

if [ "$NO_TOKEN_CODE" = "401" ]; then
    success "已开启 (401 Unauthorized)"
else
    warn "未开启或配置异常 (HTTP: $NO_TOKEN_CODE)"
fi

# 5. 检查后端 Admin API 连接
echo -n "5. Admin 后端 IM 配置: "
if docker ps --format '{{.Names}}' | grep -q "mini-customer-service-backend"; then
    ENV_URL=$(docker exec mini-customer-service-backend env 2>/dev/null | grep WUKONGIM_API_URL | cut -d= -f2)
    ENV_KEY=$(docker exec mini-customer-service-backend env 2>/dev/null | grep WUKONGIM_APP_KEY | cut -d= -f2)
    
    if [ "$ENV_URL" = "http://wukongim:5001" ] && [ -n "$ENV_KEY" ]; then
        success "正确 ($ENV_URL)"
    else
        error "配置错误 (URL: $ENV_URL, KEY: ${ENV_KEY:+已设置}${ENV_KEY:-未设置})"
        ERRORS=$((ERRORS + 1))
    fi
else
    warn "Admin 后端未运行"
fi

# 6. 检查 Portal API 连接
echo -n "6. Portal 后端 IM 配置: "
if docker ps --format '{{.Names}}' | grep -q "mini-customer-service-portal-api"; then
    ENV_URL=$(docker exec mini-customer-service-portal-api env 2>/dev/null | grep WUKONGIM_API_URL | cut -d= -f2)
    ENV_KEY=$(docker exec mini-customer-service-portal-api env 2>/dev/null | grep WUKONGIM_APP_KEY | cut -d= -f2)
    
    if [ "$ENV_URL" = "http://wukongim:5001" ] && [ -n "$ENV_KEY" ]; then
        success "正确 ($ENV_URL)"
    else
        error "配置错误 (URL: $ENV_URL, KEY: ${ENV_KEY:+已设置}${ENV_KEY:-未设置})"
        ERRORS=$((ERRORS + 1))
    fi
else
    warn "Portal 后端未运行"
fi

# 7. 检查前端配置
echo -n "7. 前端 WebSocket 配置: "
if docker ps --format '{{.Names}}' | grep -q "mini-customer-service-frontend"; then
    success "前端容器运行中"
else
    warn "前端容器未运行"
fi

echo ""
echo "=== 检查完成 ==="

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}所有检查通过!${NC}"
    exit 0
else
    echo -e "${RED}发现 $ERRORS 个错误${NC}"
    exit 1
fi
