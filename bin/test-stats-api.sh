#!/bin/bash

# 测试新增的统计和客服列表API

BASE_URL="http://localhost"
PROJECT_ID=1

echo "======================================"
echo "测试统计数据API"
echo "======================================"

echo -e "\n1. 获取统计数据 (GET /api/admin/conversations/statistics)"
curl -s "${BASE_URL}/api/admin/conversations/statistics?projectId=${PROJECT_ID}" | python3 -m json.tool

echo -e "\n\n======================================"
echo "测试客服列表API"
echo "======================================"

echo -e "\n2. 获取客服列表 (GET /api/admin/users/agents)"
curl -s "${BASE_URL}/api/admin/users/agents?projectId=${PROJECT_ID}" | python3 -m json.tool

echo -e "\n\n======================================"
echo "测试排队列表API (已存在)"
echo "======================================"

echo -e "\n3. 获取排队会话列表 (GET /api/admin/conversations/pending)"
curl -s "${BASE_URL}/api/admin/conversations/pending?projectId=${PROJECT_ID}" | python3 -m json.tool

echo -e "\n\n======================================"
echo "测试客服会话列表API (已存在)"
echo "======================================"

echo -e "\n4. 获取客服会话列表 (GET /api/admin/conversations/my)"
AGENT_ID=5  # agent1的ID
curl -s "${BASE_URL}/api/admin/conversations/my?agentId=${AGENT_ID}" | python3 -m json.tool

echo -e "\n\n======================================"
echo "测试完成"
echo "======================================"
