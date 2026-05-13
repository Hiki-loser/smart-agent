package com.smartagent.knowledge.parser;

import java.io.InputStream;

/**
 * 文档解析器接口
 * 定义文档解析的通用方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public interface DocumentParser {

    /**
     * 解析文档
     *
     * @param inputStream 文档输入流
     * @return 解析后的纯文本
     * @throws Exception 解析异常
     */
    String parse(InputStream inputStream) throws Exception;

    /**
     * 支持的文件类型
     *
     * @param fileType 文件类型（扩展名）
     * @return 是否支持
     */
    boolean support(String fileType);
}