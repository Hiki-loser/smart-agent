#!/bin/bash
# ==============================================
# SmartAgent 服务启动脚本
# 用法:
#   bash env/start.sh              # 启动所有核心服务
#   bash env/start.sh --profile milvus  # 启动含 Milvus
#   bash env/start.sh --scale chat=2    # 聊天服务扩容2实例
# ==============================================
set -e

COMPOSE_FILE="$(dirname "$0")/docker-compose.yml"
ENV_FILE="$(dirname "$0")/.env"

cd "$(dirname "$0")/.."

# 解析参数
COMPOSE_ARGS=""
SCALE_ARGS=""
while [ $# -gt 0 ]; do
  case "$1" in
    --profile)
      COMPOSE_ARGS="$COMPOSE_ARGS --profile $2"
      shift 2
      ;;
    --scale)
      SCALE_ARGS="$SCALE_ARGS --scale $2"
      shift 2
      ;;
    *)
      echo "未知参数: $1"
      echo "用法: bash env/start.sh [--profile milvus] [--scale chat=N]"
      exit 1
      ;;
  esac
done

# 检查 .env 文件
if [ ! -f "$ENV_FILE" ]; then
  echo "[WARN] 未找到 env/.env 文件，使用默认配置"
  echo "可以复制 env/.env 并修改其中的密码等配置项"
fi

echo "=== 启动 SmartAgent 服务 ==="
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d $COMPOSE_ARGS $SCALE_ARGS

echo ""
echo "服务启动完成，等待就绪..."
echo ""
echo "访问地址: http://localhost:${FRONTEND_PORT:-80}"
echo ""
echo "查看日志: docker compose -f env/docker-compose.yml logs -f [服务名]"
echo "查看状态: docker compose -f env/docker-compose.yml ps"
echo "停止服务: bash env/stop.sh"
