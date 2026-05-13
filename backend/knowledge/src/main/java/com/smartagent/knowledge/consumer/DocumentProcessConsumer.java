package com.smartagent.knowledge.consumer;

import com.smartagent.common.event.DocumentUploadEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 文档处理消费者
 * 负责接收并处理文档处理完成事件
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "document-process-topic", consumerGroup = "document-process-consumer-group")
public class DocumentProcessConsumer implements RocketMQListener<DocumentUploadEvent> {

    @Override
    public void onMessage(DocumentUploadEvent event) {
        try {
            log.info("Received document process event: documentId={}, knowledgeBaseId={}", event.getDocumentId(), event.getKnowledgeBaseId());
            
            // 处理文档处理完成事件
            // 这里可以根据业务需求进行处理，例如：
            // 1. 更新文档状态
            // 2. 更新知识库文档计数
            // 3. 触发相关业务逻辑
            
            log.info("Processing document process event: {}", event.getName());
            
        } catch (Exception e) {
            log.error("Failed to process document process event: {}", e.getMessage(), e);
        }
    }
}
