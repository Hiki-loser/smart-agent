package com.smartagent.memory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "memory")
public class MemoryProperties {

    /**
     * 摘要触发轮次阈值（超过此阈值触发异步摘要）
     */
    private Summary summary = new Summary();

    /**
     * 上下文构建配置
     */
    private Context context = new Context();

    @Data
    public static class Summary {
        private int triggerRoundThreshold = 8;
    }

    @Data
    public static class Context {
        private int recentMessageLimit = 12;
    }
}
