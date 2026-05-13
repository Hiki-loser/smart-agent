package com.smartagent.memory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smartagent")
@MapperScan("com.smartagent.memory.mapper")
public class SmartAgentMemoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAgentMemoryApplication.class, args);
    }
}
