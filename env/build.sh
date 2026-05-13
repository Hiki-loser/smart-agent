#!/bin/bash
# ==============================================
# SmartAgent Docker 镜像构建脚本
# 用法: bash env/build.sh [服务名...]
#   - 无参数: 构建所有服务
#   - gateway | user | chat | frontend: 构建指定服务
# ==============================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

build_gateway() {
  echo ">>> 构建 Gateway 网关服务..."
  docker build -f env/Dockerfile.gateway -t smart-agent-gateway:latest .
  echo ">>> Gateway 构建完成"
}

build_user() {
  echo ">>> 构建 User 用户服务..."
  docker build -f env/Dockerfile.user -t smart-agent-user:latest .
  echo ">>> User 构建完成"
}

build_chat() {
  echo ">>> 构建 Chat 聊天服务..."
  docker build -f env/Dockerfile.chat -t smart-agent-chat:latest .
  echo ">>> Chat 构建完成"
}

build_frontend() {
  echo ">>> 构建 Frontend 前端..."
  docker build -f env/Dockerfile.frontend -t smart-agent-frontend:latest .
  echo ">>> Frontend 构建完成"
}

# 如果有参数，构建指定服务；否则构建全部
if [ $# -gt 0 ]; then
  for svc in "$@"; do
    case "$svc" in
      gateway)  build_gateway ;;
      user)     build_user ;;
      chat)     build_chat ;;
      frontend) build_frontend ;;
      *)
        echo "未知服务: $svc (可选: gateway, user, chat, frontend)"
        exit 1
        ;;
    esac
  done
else
  echo "开始构建所有 SmartAgent 镜像..."
  build_gateway
  build_user
  build_chat
  build_frontend
  echo ""
  echo "========================================"
  echo "所有镜像构建完成:"
  echo "  - smart-agent-gateway:latest"
  echo "  - smart-agent-user:latest"
  echo "  - smart-agent-chat:latest"
  echo "  - smart-agent-frontend:latest"
  echo "========================================"
fi
