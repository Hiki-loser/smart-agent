package com.smartagent.knowledge.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;

/**
 * Word文档解析器
 * 使用Apache POI解析Word文档
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
public class WordDocumentParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        try {
            // 尝试解析.docx文件
            try (XWPFDocument document = new XWPFDocument(inputStream)) {
                XWPFWordExtractor extractor = new XWPFWordExtractor(document);
                return extractor.getText();
            } catch (Exception e) {
                // 如果不是.docx文件，尝试解析.doc文件
                try (HWPFDocument document = new HWPFDocument(inputStream)) {
                    WordExtractor extractor = new WordExtractor(document);
                    return extractor.getText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Word document: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean support(String fileType) {
        return "docx".equalsIgnoreCase(fileType) || "doc".equalsIgnoreCase(fileType);
    }
}