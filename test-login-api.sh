#!/bin/bash

# 登录API测试脚本

echo "======================================"
echo "  客服系统登录API测试"
echo "======================================"
echo ""

# 获取验证码
echo "[1/3] 获取验证码..."
CAPTCHA_RESPONSE=$(curl -s http://localhost/api/admin/auth/captcha)
echo "响应: $CAPTCHA_RESPONSE"
echo ""

# 解析验证码
QUESTION=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['question']) if data['code']==0 else exit(1)")
KEY=$(echo $CAPTCHA_RESPONSE | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['key']) if data['code']==0 else exit(1)")

if [ -z "$KEY" ]; then
    echo "❌ 获取验证码失败"
    exit 1
fi

echo "📝 验证码问题: $QUESTION"
echo "🔑 验证码KEY: $KEY"
echo ""

# 计算答案
ANSWER=$(python3 << EOF
import re
question = "$QUESTION"
nums = list(map(int, re.findall(r'\d+', question)))
if '+' in question:
    print(nums[0] + nums[1])
elif '-' in question:
    print(nums[0] - nums[1])
elif '×' in question or '*' in question:
    print(nums[0] * nums[1])
EOF
)

echo "✅ 计算答案: $ANSWER"
echo ""

# 测试admin登录
echo "[2/3] 测试admin登录..."
echo "--------------------------------------"
ADMIN_RESULT=$(curl -s -X POST http://localhost/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"captcha\":\"$ANSWER\",\"captchaKey\":\"$KEY\"}")

echo "$ADMIN_RESULT" | python3 -m json.tool

ADMIN_ROLE=$(echo "$ADMIN_RESULT" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['user']['role']) if data['code']==0 else exit(1)" 2>/dev/null)
ADMIN_ID=$(echo "$ADMIN_RESULT" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['user']['id']) if data['code']==0 else exit(1)" 2>/dev/null)

if [ "$ADMIN_ROLE" == "admin" ]; then
    echo "✅ admin登录成功 (ID: $ADMIN_ID, Role: $ADMIN_ROLE)"
else
    echo "❌ admin登录失败或角色不正确"
fi
echo ""

# 获取新的验证码用于agent1测试
echo "[3/3] 测试agent1登录..."
echo "--------------------------------------"
CAPTCHA_RESPONSE2=$(curl -s http://localhost/api/admin/auth/captcha)
QUESTION2=$(echo $CAPTCHA_RESPONSE2 | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['question']) if data['code']==0 else exit(1)")
KEY2=$(echo $CAPTCHA_RESPONSE2 | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['key']) if data['code']==0 else exit(1)")

echo "📝 验证码问题: $QUESTION2"

ANSWER2=$(python3 << EOF
import re
question = "$QUESTION2"
nums = list(map(int, re.findall(r'\d+', question)))
if '+' in question:
    print(nums[0] + nums[1])
elif '-' in question:
    print(nums[0] - nums[1])
elif '×' in question or '*' in question:
    print(nums[0] * nums[1])
EOF
)

echo "✅ 计算答案: $ANSWER2"
echo ""

AGENT_RESULT=$(curl -s -X POST http://localhost/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"agent1\",\"password\":\"admin123\",\"captcha\":\"$ANSWER2\",\"captchaKey\":\"$KEY2\"}")

echo "$AGENT_RESULT" | python3 -m json.tool

AGENT_ROLE=$(echo "$AGENT_RESULT" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['user']['role']) if data['code']==0 else exit(1)" 2>/dev/null)
AGENT_ID=$(echo "$AGENT_RESULT" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['data']['user']['id']) if data['code']==0 else exit(1)" 2>/dev/null)

if [ "$AGENT_ROLE" == "agent" ]; then
    echo "✅ agent1登录成功 (ID: $AGENT_ID, Role: $AGENT_ROLE)"
else
    echo "❌ agent1登录失败或角色不正确"
fi

echo ""
echo "======================================"
echo "  测试完成"
echo "======================================"
