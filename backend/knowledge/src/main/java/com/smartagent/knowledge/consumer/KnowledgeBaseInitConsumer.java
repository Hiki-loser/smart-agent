package com.smartagent.knowledge.consumer;

import com.smartagent.common.event.KnowledgeBaseCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 消息消费者
 * 负责接收并处理来自 RocketMQ 的知识库相关消息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "knowledge-base-init-topic", consumerGroup = "knowledge-base-init-consumer-group")
public class KnowledgeBaseInitConsumer implements RocketMQListener<KnowledgeBaseCreateEvent> {

    @Override
    public void onMessage(KnowledgeBaseCreateEvent event) {
        try {
            log.info("Received knowledge base init event: knowledgeBaseId={}, userId={}", event.getKnowledgeBaseId(), event.getUserId());
            
            // 处理知识库初始化完成事件
            // 这里可以根据业务需求进行处理，例如：
            // 1. 更新知识库状态
            // 2. 触发相关业务逻辑
            
            log.info("Processing knowledge base init event: {}", event.getName());
            
        } catch (Exception e) {
            log.error("Failed to process knowledge base init event: {}", e.getMessage(), e);
        }
    }
}
