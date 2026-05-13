package com.smartagent.knowledge.pipeline;

import com.smartagent.knowledge.entity.DocumentEntity;
import com.smartagent.knowledge.mapper.DocumentMapper;
import com.smartagent.knowledge.parser.DocumentParserFactory;
import com.smartagent.knowledge.splitter.TextSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库构建流水线
 * 串联"解析 → 切片 → 向量化 → 入库"全流程
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
public class KnowledgeBuildPipeline {

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    private final TextSplitter textSplitter = new TextSplitter();

    /**
     * 处理文档
     *
     * @param docId 文档ID
     * @param inputStream 文档输入流
     * @param fileType 文件类型
     * @param knowledgeBaseId 知识库ID
     * @throws Exception 处理异常
     */
    public void process(Long docId, InputStream inputStream, String fileType, Long knowledgeBaseId) throws Exception {
        log.info("Processing document: docId={}, knowledgeBaseId={}, fileType={}", docId, knowledgeBaseId, fileType);

        try {
            // 1. 查DB获取文档信息，更新status=1(处理中)
            DocumentEntity document = documentMapper.selectById(docId);
            if (document == null) {
                throw new IllegalArgumentException("Document not found: " + docId);
            }
            document.setStatus(1); // 处理中
            documentMapper.updateById(document);

            // 2. 解析文档
            String text = parseDocument(inputStream, fileType);
            log.info("Document parsed successfully, length: {}", text.length());

            // 3. 文本切片
            List<TextSegment> segments = splitText(text, docId, knowledgeBaseId);
            log.info("Text split into {} segments", segments.size());

            // 4. 向量化并入库
            embedAndStore(segments);
            log.info("Segments embedded and stored successfully");

            // 5. 更新DB：status=2，chunk_count=N
            document.setStatus(2); // 完成
            document.setChunkCount(segments.size());
            documentMapper.updateById(document);

            log.info("Document processing completed: docId={}", docId);

        } catch (Exception e) {
            log.error("Failed to process document: {}", e.getMessage(), e);
            // 更新DB：status=-1，记录 error_msg
            DocumentEntity document = documentMapper.selectById(docId);
            if (document != null) {
                document.setStatus(-1); // 失败
                document.setErrorMsg(e.getMessage());
                documentMapper.updateById(document);
            }
            throw e;
        }
    }

    /**
     * 解析文档
     *
     * @param inputStream 文档输入流
     * @param fileType 文件类型
     * @return 解析后的纯文本
     * @throws Exception 解析异常
     */
    private String parseDocument(InputStream inputStream, String fileType) throws Exception {
        var parser = DocumentParserFactory.getParser(fileType);
        return parser.parse(inputStream);
    }

    /**
     * 文本切片
     *
     * @param text 文本内容
     * @param docId 文档ID
     * @param knowledgeBaseId 知识库ID
     * @return 文本片段列表
     */
    private List<TextSegment> splitText(String text, Long docId, Long knowledgeBaseId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", docId);
        metadata.put("knowledgeBaseId", knowledgeBaseId);
        return textSplitter.split(text, metadata);
    }

    /**
     * 向量化并入库
     *
     * @param segments 文本片段列表
     * @throws Exception 向量化异常
     */
    private void embedAndStore(List<TextSegment> segments) {
        var response = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = response.content();
        embeddingStore.addAll(embeddings, segments);
    }
}