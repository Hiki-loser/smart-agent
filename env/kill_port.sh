#!/bin/bash

# 定义要清理的端口范围：8080 到 8090
# 您可以修改这里的范围，例如 {8080..8099}
PORTS=$(seq 8080 8090)

echo "正在扫描端口: 8080-8090 ..."

for port in $PORTS; do
    # 查找占用指定端口的 PID
    # -t: 仅显示 PID，不显示头部信息
    # 2>/dev/null: 屏蔽 "没有找到进程" 的报错信息
    PID=$(lsof -t -i:$port 2>/dev/null)

    if [ -n "$PID" ]; then
        echo "发现端口 $port 被进程占用 (PID: $PID)，正在尝试杀死..."

        # 尝试杀死进程
        # 如果是当前用户启动的进程，直接 kill；如果权限不足可能需要 sudo
        kill -9 $PID 2>/dev/null

        # 检查是否杀死成功
        if [ $? -eq 0 ]; then
            echo -e "\033[32m [成功] 端口 $port 进程已杀死 \033[0m"
        else
            echo -e "\033[31m [失败] 无法杀死端口 $port 进程 (可能需要 root 权限) \033[0m"
        fi
    else
        # 可选：输出未占用的提示，如果觉得输出太多，可以注释掉这一行
        echo "端口 $port 未被占用"
    fi
done

echo "操作完成。"
