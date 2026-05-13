package com.smartagent.chat.producer;

import com.smartagent.common.event.MessageArchiveEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class RocketMQProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectProvider<DefaultMQProducer> defaultMQProducerProvider;

    @Value("${chat.mq.topic.message-archive:message-archive-topic}")
    private String messageArchiveTopic;

    public void sendMessageArchiveEvent(MessageArchiveEvent event) {
        if (!isProducerReady("message archive")) {
            return;
        }
        try {
            rocketMQTemplate.convertAndSend(messageArchiveTopic, event);
            log.info("Sent message archive event: sessionId={}, userId={}", event.getSessionId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send message archive event: {}", e.getMessage(), e);
        }
    }

    private boolean isProducerReady(String eventType) {
        DefaultMQProducer producer = defaultMQProducerProvider.getIfAvailable();
        if (producer == null) {
            log.warn("Skip {} event send because DefaultMQProducer is unavailable", eventType);
            return false;
        }
        if (rocketMQTemplate.getProducer() == null) {
            log.warn("Skip {} event send because RocketMQTemplate producer is not initialized", eventType);
            return false;
        }
        return true;
    }
}
