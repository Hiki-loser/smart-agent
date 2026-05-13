package com.smartagent.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * SmartAgent 聊天服务应用
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.smartagent")
@MapperScan("com.smartagent.chat.mapper")
@EnableFeignClients(basePackages = "com.smartagent.chat.feign")
public class SmartAgentChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAgentChatApplication.class, args);
    }

}
