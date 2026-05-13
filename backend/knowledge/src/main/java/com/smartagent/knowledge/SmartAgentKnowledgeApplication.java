package com.smartagent.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.smartagent")
@EnableDiscoveryClient
public class SmartAgentKnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAgentKnowledgeApplication.class, args);
    }

}
