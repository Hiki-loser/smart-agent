package com.smartagent.knowledge.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析器工厂
 * 根据文件类型选择对应的解析器
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public class DocumentParserFactory {

    private static final List<DocumentParser> parsers = new ArrayList<>();

    static {
        // 注册所有解析器
        parsers.add(new PdfDocumentParser());
        parsers.add(new WordDocumentParser());
        parsers.add(new TxtDocumentParser());
        // 可以添加更多解析器
    }

    /**
     * 获取适合的文档解析器
     *
     * @param fileType 文件类型（扩展名）
     * @return 文档解析器
     * @throws IllegalArgumentException 如果没有找到适合的解析器
     */
    public static DocumentParser getParser(String fileType) {
        for (DocumentParser parser : parsers) {
            if (parser.support(fileType)) {
                return parser;
            }
        }
        throw new IllegalArgumentException("No parser found for file type: " + fileType);
    }

    /**
     * 注册新的文档解析器
     *
     * @param parser 文档解析器
     */
    public static void registerParser(DocumentParser parser) {
        parsers.add(parser);
    }
}