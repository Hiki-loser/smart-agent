package com.smartagent.knowledge.parser;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * TXT文档解析器
 * 直接读取文本内容
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
public class TxtDocumentParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            log.error("Failed to parse TXT document: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean support(String fileType) {
        return "txt".equalsIgnoreCase(fileType);
    }
}