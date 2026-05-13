package com.smartagent.knowledge.consumer;

import com.smartagent.common.event.KnowledgeQueryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 知识库查询结果消费者
 * 负责接收并处理知识库查询结果事件
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "knowledge-query-result-topic", consumerGroup = "knowledge-query-result-consumer-group")
public class KnowledgeQueryResultConsumer implements RocketMQListener<KnowledgeQueryEvent> {

    @Override
    public void onMessage(KnowledgeQueryEvent event) {
        try {
            log.info("Received knowledge query result event: queryId={}, knowledgeBaseId={}", event.getQueryId(), event.getKnowledgeBaseId());
            // TODO: 需要新增结果持久化/缓存组件（如 Redis Repository）保存 queryId -> result，供 queryKnowledgeBase 同步读取。
            // TODO: 当前事件体仅包含查询参数，后续需补充核心服务生成的 answer/fragments 字段或定义新的 ResultEvent。
            
            // 处理知识库查询结果事件
            // 这里可以根据业务需求进行处理，例如：
            // 1. 存储查询结果
            // 2. 通知前端查询完成
            // 3. 触发相关业务逻辑
            
            log.info("Processing knowledge query result event: {}", event.getQuery());
            
        } catch (Exception e) {
            log.error("Failed to process knowledge query result event: {}", e.getMessage(), e);
        }
    }
}
