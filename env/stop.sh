#!/bin/bash
# ==============================================
# SmartAgent 服务停止脚本
# 用法:
#   bash env/stop.sh              # 停止所有服务
#   bash env/stop.sh --volumes    # 停止并删除数据卷（重置所有数据）
# ==============================================
set -e

COMPOSE_FILE="$(dirname "$0")/docker-compose.yml"

cd "$(dirname "$0")/.."

echo "=== 停止 SmartAgent 服务 ==="

if [ "$1" = "--volumes" ]; then
  echo "警告: 将删除所有数据卷 (MySQL/Redis/ES 数据将被清空)!"
  echo -n "确认? (yes/no): "
  read -r confirm
  if [ "$confirm" = "yes" ]; then
    docker compose -f "$COMPOSE_FILE" down -v
    echo "服务已停止，数据卷已删除"
  else
    echo "已取消"
  fi
else
  docker compose -f "$COMPOSE_FILE" down
  echo "服务已停止（数据卷已保留，下次启动数据仍在）"
fi
