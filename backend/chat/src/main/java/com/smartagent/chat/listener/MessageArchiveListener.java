package com.smartagent.chat.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.smartagent.common.event.MessageArchiveEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息归档监听器
 * 消费消息归档事件并写入 Elasticsearch
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${chat.mq.topic.message-archive:message-archive-topic}",
        consumerGroup = "${chat.mq.consumer.message-archive-group:message-archive-consumer-group}"
)
public class MessageArchiveListener implements RocketMQListener<MessageArchiveEvent> {

    private final ElasticsearchClient elasticsearchClient;
    private final String archiveIndex;

    public MessageArchiveListener(ElasticsearchClient elasticsearchClient,
                                  @Value("${chat.archive.elasticsearch.index:chat-message-archive}") String archiveIndex) {
        this.elasticsearchClient = elasticsearchClient;
        this.archiveIndex = archiveIndex;
    }

    @Override
    public void onMessage(MessageArchiveEvent event) {
        try {
            log.info("Received message archive event: sessionId={}, userId={}", event.getSessionId(), event.getUserId());

            Map<String, Object> document = new HashMap<>();
            document.put("sessionId", event.getSessionId());
            document.put("content", event.getContent());
            document.put("userId", event.getUserId());
            document.put("aiResponse", event.getAiResponse());
            document.put("timestamp", Instant.now().toString());

            String documentId = event.getSessionId() + "-" + System.currentTimeMillis();
            IndexResponse response = elasticsearchClient.index(req -> req
                    .index(archiveIndex)
                    .id(documentId)
                    .document(document));

            log.info("Archived message to Elasticsearch: index={}, documentId={}, result={}",
                    archiveIndex, documentId, response.result());

        } catch (Exception e) {
            log.error("Failed to archive message: {}", e.getMessage(), e);
        }
    }
}
