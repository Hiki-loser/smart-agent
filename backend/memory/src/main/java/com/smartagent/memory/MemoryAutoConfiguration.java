package com.smartagent.memory;

import com.smartagent.memory.config.MemoryProperties;
import com.smartagent.memory.service.MemoryService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnClass(MemoryService.class)
@EnableConfigurationProperties(MemoryProperties.class)
@MapperScan("com.smartagent.memory.mapper")
@ComponentScan(basePackages = "com.smartagent.memory")
public class MemoryAutoConfiguration {
}
