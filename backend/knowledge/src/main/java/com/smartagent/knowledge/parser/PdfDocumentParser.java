package com.smartagent.knowledge.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


import java.io.InputStream;

/**
 * PDF文档解析器
 * 使用Apache PDFBox解析PDF文档
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
public class PdfDocumentParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("Failed to parse PDF document: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean support(String fileType) {
        return "pdf".equalsIgnoreCase(fileType);
    }
}