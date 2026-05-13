package com.smartagent.knowledge.producer;

import com.smartagent.common.event.DocumentUploadEvent;
import com.smartagent.common.event.KnowledgeBaseCreateEvent;
import com.smartagent.common.event.KnowledgeQueryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * RocketMQ 消息生产者
 * 负责发送知识库相关的消息到 RocketMQ
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
public class RocketMQProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送知识库创建事件
     *
     * @param event 知识库创建事件
     */
    public void sendKnowledgeBaseCreateEvent(KnowledgeBaseCreateEvent event) {
        try {
            rocketMQTemplate.convertAndSend("knowledge-base-create-topic", event);
            log.info("Sent knowledge base create event: knowledgeBaseId={}, userId={}", event.getKnowledgeBaseId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send knowledge base create event: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送文档上传事件
     *
     * @param event 文档上传事件
     */
    public void sendDocumentUploadEvent(DocumentUploadEvent event) {
        try {
            rocketMQTemplate.convertAndSend("document-upload-topic", event);
            log.info("Sent document upload event: documentId={}, knowledgeBaseId={}", event.getDocumentId(), event.getKnowledgeBaseId());
        } catch (Exception e) {
            log.error("Failed to send document upload event: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送知识库查询事件
     *
     * @param event 知识库查询事件
     */
    public void sendKnowledgeQueryEvent(KnowledgeQueryEvent event) {
        try {
            rocketMQTemplate.convertAndSend("knowledge-query-topic", event);
            log.info("Sent knowledge query event: queryId={}, knowledgeBaseId={}", event.getQueryId(), event.getKnowledgeBaseId());
        } catch (Exception e) {
            log.error("Failed to send knowledge query event: {}", e.getMessage(), e);
        }
    }
}
