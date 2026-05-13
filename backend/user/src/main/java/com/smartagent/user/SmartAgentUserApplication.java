package com.smartagent.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * SmartAgent 用户服务应用
 *
 * @author SmartAgent
 * @since 1.0.0
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.smartagent.common", "com.smartagent.user"})
public class SmartAgentUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAgentUserApplication.class, args);
    }

}
