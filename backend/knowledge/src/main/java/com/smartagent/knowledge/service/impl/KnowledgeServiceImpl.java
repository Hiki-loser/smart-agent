package com.smartagent.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.knowledge.dto.CreateKnowledgeBaseDTO;
import com.smartagent.knowledge.dto.QueryDTO;
import com.smartagent.knowledge.entity.DocumentEntity;
import com.smartagent.knowledge.entity.KnowledgeBaseEntity;
import com.smartagent.common.event.DocumentUploadEvent;
import com.smartagent.common.event.KnowledgeBaseCreateEvent;
import com.smartagent.common.event.KnowledgeQueryEvent;
import com.smartagent.knowledge.mapper.DocumentMapper;
import com.smartagent.knowledge.mapper.KnowledgeBaseMapper;
import com.smartagent.knowledge.producer.RocketMQProducer;
import com.smartagent.knowledge.service.KnowledgeService;
import com.smartagent.knowledge.vo.DocumentVO;
import com.smartagent.knowledge.vo.KnowledgeBaseVO;
import com.smartagent.knowledge.vo.QueryResultVO;
import com.smartagent.common.utils.UserContextUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识库服务实现类
 * 实现知识库相关的核心业务逻辑，支持 Milvus 和 Elasticsearch 双向量后端
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private RocketMQProducer rocketMQProducer;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Value("${vector-store.type:elasticsearch}")
    private String vectorStoreType;

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional
    public KnowledgeBaseVO createKnowledgeBase(CreateKnowledgeBaseDTO dto) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = new KnowledgeBaseEntity();
        knowledgeBase.setUserId(userId);
        knowledgeBase.setName(dto.getName());
        knowledgeBase.setDescription(dto.getDescription());
        knowledgeBase.setStatus(0);
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());

        knowledgeBaseMapper.insert(knowledgeBase);

        KnowledgeBaseCreateEvent event = new KnowledgeBaseCreateEvent();
        event.setKnowledgeBaseId(knowledgeBase.getId());
        event.setUserId(userId);
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        rocketMQProducer.sendKnowledgeBaseCreateEvent(event);

        KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);

        return knowledgeBaseVO;
    }

    @Override
    @Transactional
    public DocumentVO uploadDocument(Long knowledgeBaseId, MultipartFile file) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".bin";
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            DocumentEntity document = new DocumentEntity();
            document.setKnowledgeBaseId(knowledgeBaseId);
            document.setName(originalFilename);
            document.setType(file.getContentType());
            document.setSize(file.getSize());
            document.setStatus(1);
            document.setStoragePath(filePath.toString());
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());

            documentMapper.insert(document);

            knowledgeBase.setDocumentCount(knowledgeBase.getDocumentCount() + 1);
            knowledgeBase.setUpdatedAt(LocalDateTime.now());
            knowledgeBaseMapper.updateById(knowledgeBase);

            DocumentUploadEvent event = new DocumentUploadEvent();
            event.setDocumentId(document.getId());
            event.setKnowledgeBaseId(knowledgeBaseId);
            event.setUserId(userId);
            event.setName(originalFilename);
            event.setType(file.getContentType());
            event.setSize(file.getSize());
            event.setStoragePath(filePath.toString());
            rocketMQProducer.sendDocumentUploadEvent(event);

            DocumentVO documentVO = new DocumentVO();
            BeanUtils.copyProperties(document, documentVO);

            return documentVO;
        } catch (IOException e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            throw new RuntimeException("上传文档失败");
        }
    }

    @Override
    public QueryResultVO queryKnowledgeBase(QueryDTO dto) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(dto.getKnowledgeBaseId());
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        String queryId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        log.info("Executing vector search for query: [{}] in knowledgeBaseId={}, topK={}, threshold={}",
                dto.getQuery(), dto.getKnowledgeBaseId(), dto.getTopK(), dto.getSimilarityThreshold());

        try {
            // 1. 将查询文本向量化
            Embedding queryEmbedding = embeddingModel.embed(dto.getQuery()).content();

            // 2. 构建向量搜索请求（按知识库ID过滤）
            int topK = dto.getTopK() != null ? dto.getTopK() : 5;
            double minScore = dto.getSimilarityThreshold() != null ? dto.getSimilarityThreshold() : 0.7;

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(minScore)
                    .filter(new IsEqualTo("metadata.knowledgeBaseId", dto.getKnowledgeBaseId()))
                    .build();

            // 3. 执行向量搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();

            // 4. 构建返回结果
            QueryResultVO resultVO = new QueryResultVO();
            resultVO.setQueryId(queryId);
            resultVO.setQuery(dto.getQuery());
            resultVO.setExecutionTime(System.currentTimeMillis() - startTime);

            List<QueryResultVO.DocumentFragmentVO> fragments = matches.stream()
                    .map(match -> {
                        QueryResultVO.DocumentFragmentVO fragment = new QueryResultVO.DocumentFragmentVO();
                        TextSegment segment = match.embedded();
                        if (segment != null) {
                            fragment.setContent(segment.text());
                            fragment.setSimilarity(match.score());
                            Object docId = segment.metadata().get("documentId");
                            if (docId instanceof Number) {
                                fragment.setDocumentId(((Number) docId).longValue());
                            }
                            Object docName = segment.metadata().get("documentName");
                            if (docName != null) {
                                fragment.setDocumentName(docName.toString());
                            }
                            Object pageNum = segment.metadata().get("pageNumber");
                            if (pageNum instanceof Number) {
                                fragment.setPageNumber(((Number) pageNum).intValue());
                            }
                        }
                        return fragment;
                    })
                    .collect(Collectors.toList());

            resultVO.setDocumentFragments(fragments);
            resultVO.setAnswer(fragments.isEmpty()
                    ? "未找到相关文档内容"
                    : "找到 " + fragments.size() + " 个相关片段");

            // 5. 发送 MQ 事件用于异步归档
            KnowledgeQueryEvent event = new KnowledgeQueryEvent();
            event.setQueryId(queryId);
            event.setKnowledgeBaseId(dto.getKnowledgeBaseId());
            event.setUserId(userId);
            event.setQuery(dto.getQuery());
            event.setSimilarityThreshold(dto.getSimilarityThreshold());
            event.setTopK(dto.getTopK());
            rocketMQProducer.sendKnowledgeQueryEvent(event);

            return resultVO;

        } catch (Exception e) {
            log.error("Vector search failed: {}", e.getMessage(), e);
            QueryResultVO errorVO = new QueryResultVO();
            errorVO.setQueryId(queryId);
            errorVO.setQuery(dto.getQuery());
            errorVO.setAnswer("向量检索失败: " + e.getMessage());
            errorVO.setExecutionTime(System.currentTimeMillis() - startTime);
            errorVO.setDocumentFragments(new ArrayList<>());
            return errorVO;
        }
    }

    @Override
    public List<KnowledgeBaseVO> getKnowledgeBaseList() {
        Long userId = UserContextUtils.getUserId();

        LambdaQueryWrapper<KnowledgeBaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseEntity::getUserId, userId);
        List<KnowledgeBaseEntity> knowledgeBaseList = knowledgeBaseMapper.selectList(wrapper);

        return knowledgeBaseList.stream().map(entity -> {
            KnowledgeBaseVO vo = new KnowledgeBaseVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public KnowledgeBaseVO getKnowledgeBaseDetail(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, vo);
        return vo;
    }

    @Override
    public List<DocumentVO> getDocumentList(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId);
        List<DocumentEntity> documentList = documentMapper.selectList(wrapper);

        return documentList.stream().map(entity -> {
            DocumentVO vo = new DocumentVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteKnowledgeBase(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId);
        documentMapper.delete(wrapper);
        knowledgeBaseMapper.deleteById(knowledgeBaseId);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteDocument(Long documentId) {
        DocumentEntity document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(document.getKnowledgeBaseId());
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(UserContextUtils.getUserId())) {
            throw new RuntimeException("无权限");
        }

        try {
            Files.deleteIfExists(Paths.get(document.getStoragePath()));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", document.getStoragePath(), e);
        }

        documentMapper.deleteById(documentId);

        knowledgeBase.setDocumentCount(Math.max(0, knowledgeBase.getDocumentCount() - 1));
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.updateById(knowledgeBase);

        return true;
    }

    @Override
    public Map<String, Object> getVectorStoreInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("type", vectorStoreType);
        info.put("embeddingStoreClass", embeddingStore.getClass().getSimpleName());
        return info;
    }
}
