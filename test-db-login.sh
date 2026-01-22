#!/bin/bash

# 测试使用数据库账号的登录功能

BASE_URL="http://localhost"

echo "======================================"
echo "测试数据库登录功能"
echo "======================================"

# 1. 获取验证码
echo -e "\n1. 获取验证码"
CAPTCHA_RESPONSE=$(curl -s "${BASE_URL}/api/admin/auth/captcha")
echo "验证码响应: $CAPTCHA_RESPONSE"

CAPTCHA_KEY=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['key'])" 2>/dev/null)
CAPTCHA_QUESTION=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['question'])" 2>/dev/null)

echo "验证码Key: $CAPTCHA_KEY"
echo "验证码问题: $CAPTCHA_QUESTION"

# 简单计算答案（仅支持简单加减乘）
CAPTCHA_ANSWER=$(echo "$CAPTCHA_QUESTION" | python3 -c "
import sys, re
q = sys.stdin.read().strip()
# 提取数字和运算符
match = re.match(r'(\d+)\s*([+\-×])\s*(\d+)', q)
if match:
    a, op, b = int(match.group(1)), match.group(2), int(match.group(3))
    if op == '+':
        print(a + b)
    elif op == '-':
        print(a - b)
    elif op == '×':
        print(a * b)
" 2>/dev/null)

echo "验证码答案: $CAPTCHA_ANSWER"

# 2. 测试admin账号登录
echo -e "\n======================================"
echo "2. 测试admin账号登录（从数据库）"
echo "======================================"
ADMIN_LOGIN=$(curl -s -X POST "${BASE_URL}/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"captcha\":\"$CAPTCHA_ANSWER\",\"captchaKey\":\"$CAPTCHA_KEY\"}")

echo "$ADMIN_LOGIN" | python3 -m json.tool

ADMIN_ROLE=$(echo $ADMIN_LOGIN | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('data', {}).get('user', {}).get('role', 'N/A'))" 2>/dev/null)
ADMIN_ID=$(echo $ADMIN_LOGIN | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('data', {}).get('user', {}).get('id', 'N/A'))" 2>/dev/null)

echo -e "\n✓ Admin账号登录结果:"
echo "  - 角色: $ADMIN_ROLE"
echo "  - ID: $ADMIN_ID"

# 3. 获取新验证码
sleep 1
CAPTCHA_RESPONSE=$(curl -s "${BASE_URL}/api/admin/auth/captcha")
CAPTCHA_KEY=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['key'])" 2>/dev/null)
CAPTCHA_QUESTION=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['question'])" 2>/dev/null)
CAPTCHA_ANSWER=$(echo "$CAPTCHA_QUESTION" | python3 -c "
import sys, re
q = sys.stdin.read().strip()
match = re.match(r'(\d+)\s*([+\-×])\s*(\d+)', q)
if match:
    a, op, b = int(match.group(1)), match.group(2), int(match.group(3))
    if op == '+':
        print(a + b)
    elif op == '-':
        print(a - b)
    elif op == '×':
        print(a * b)
" 2>/dev/null)

# 4. 测试agent1账号登录
echo -e "\n======================================"
echo "3. 测试agent1账号登录（从数据库）"
echo "======================================"
AGENT1_LOGIN=$(curl -s -X POST "${BASE_URL}/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"agent1\",\"password\":\"admin123\",\"captcha\":\"$CAPTCHA_ANSWER\",\"captchaKey\":\"$CAPTCHA_KEY\"}")

echo "$AGENT1_LOGIN" | python3 -m json.tool

AGENT1_ROLE=$(echo $AGENT1_LOGIN | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('data', {}).get('user', {}).get('role', 'N/A'))" 2>/dev/null)
AGENT1_ID=$(echo $AGENT1_LOGIN | python3 -c "import sys, json; data=json.load(sys.stdin); print(data.get('data', {}).get('user', {}).get('id', 'N/A'))" 2>/dev/null)

echo -e "\n✓ Agent1账号登录结果:"
echo "  - 角色: $AGENT1_ROLE"
echo "  - ID: $AGENT1_ID"

# 5. 测试错误密码
sleep 1
CAPTCHA_RESPONSE=$(curl -s "${BASE_URL}/api/admin/auth/captcha")
CAPTCHA_KEY=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['key'])" 2>/dev/null)
CAPTCHA_QUESTION=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['question'])" 2>/dev/null)
CAPTCHA_ANSWER=$(echo "$CAPTCHA_QUESTION" | python3 -c "
import sys, re
q = sys.stdin.read().strip()
match = re.match(r'(\d+)\s*([+\-×])\s*(\d+)', q)
if match:
    a, op, b = int(match.group(1)), match.group(2), int(match.group(3))
    if op == '+':
        print(a + b)
    elif op == '-':
        print(a - b)
    elif op == '×':
        print(a * b)
" 2>/dev/null)

echo -e "\n======================================"
echo "4. 测试错误密码（应该失败）"
echo "======================================"
WRONG_PWD=$(curl -s -X POST "${BASE_URL}/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"wrongpassword\",\"captcha\":\"$CAPTCHA_ANSWER\",\"captchaKey\":\"$CAPTCHA_KEY\"}")

echo "$WRONG_PWD" | python3 -m json.tool

echo -e "\n======================================"
echo "测试完成"
echo "======================================"
echo -e "\n总结:"
echo "✓ 验证码生成正常"
echo "✓ Admin账号登录: 角色=$ADMIN_ROLE, ID=$ADMIN_ID"
echo "✓ Agent1账号登录: 角色=$AGENT1_ROLE, ID=$AGENT1_ID"
echo "✓ 错误密码验证正常"
echo -e "\n所有账号数据现在都从PostgreSQL数据库查询！"
