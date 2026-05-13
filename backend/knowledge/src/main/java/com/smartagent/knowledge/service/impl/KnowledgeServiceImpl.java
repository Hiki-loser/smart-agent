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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 知识库服务实现类
 * 实现知识库相关的核心业务逻辑
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

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    @Transactional
    public KnowledgeBaseVO createKnowledgeBase(CreateKnowledgeBaseDTO dto) {
        Long userId = UserContextUtils.getUserId();
        
        // 创建知识库
        KnowledgeBaseEntity knowledgeBase = new KnowledgeBaseEntity();
        knowledgeBase.setUserId(userId);
        knowledgeBase.setName(dto.getName());
        knowledgeBase.setDescription(dto.getDescription());
        knowledgeBase.setStatus(0); // 初始化中
        knowledgeBase.setDocumentCount(0);
        knowledgeBase.setCreatedAt(LocalDateTime.now());
        knowledgeBase.setUpdatedAt(LocalDateTime.now());

        knowledgeBaseMapper.insert(knowledgeBase);

        // 发送知识库创建事件
        KnowledgeBaseCreateEvent event = new KnowledgeBaseCreateEvent();
        event.setKnowledgeBaseId(knowledgeBase.getId());
        event.setUserId(userId);
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        rocketMQProducer.sendKnowledgeBaseCreateEvent(event);

        // 转换为 VO
        KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);

        return knowledgeBaseVO;
    }

    @Override
    @Transactional
    public DocumentVO uploadDocument(Long knowledgeBaseId, MultipartFile file) {
        Long userId = UserContextUtils.getUserId();
        
        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        try {
            // 确保上传目录存在
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".bin";
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 创建文档记录
            DocumentEntity document = new DocumentEntity();
            document.setKnowledgeBaseId(knowledgeBaseId);
            document.setName(originalFilename);
            document.setType(file.getContentType());
            document.setSize(file.getSize());
            document.setStatus(1); // 处理中
            document.setStoragePath(filePath.toString());
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());

            documentMapper.insert(document);

            // 更新知识库文档计数
            knowledgeBase.setDocumentCount(knowledgeBase.getDocumentCount() + 1);
            knowledgeBase.setUpdatedAt(LocalDateTime.now());
            knowledgeBaseMapper.updateById(knowledgeBase);

            // 发送文档上传事件
            DocumentUploadEvent event = new DocumentUploadEvent();
            event.setDocumentId(document.getId());
            event.setKnowledgeBaseId(knowledgeBaseId);
            event.setUserId(userId);
            event.setName(originalFilename);
            event.setType(file.getContentType());
            event.setSize(file.getSize());
            event.setStoragePath(filePath.toString());
            rocketMQProducer.sendDocumentUploadEvent(event);

            // 转换为 VO
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
        
        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(dto.getKnowledgeBaseId());
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        // 生成查询ID
        String queryId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        // 发送知识库查询事件
        KnowledgeQueryEvent event = new KnowledgeQueryEvent();
        event.setQueryId(queryId);
        event.setKnowledgeBaseId(dto.getKnowledgeBaseId());
        event.setUserId(userId);
        event.setQuery(dto.getQuery());
        event.setSimilarityThreshold(dto.getSimilarityThreshold());
        event.setTopK(dto.getTopK());
        rocketMQProducer.sendKnowledgeQueryEvent(event);

        // TODO: 当前仍是同步返回占位结果。要实现真正查询闭环，需要引入查询结果存储与回执机制
        // TODO: 建议新增依赖 spring-boot-starter-data-redis，用 queryId 作为 key 存储 core 回传结果并设置超时轮询/阻塞等待
        // 模拟查询结果（实际项目中应该等待MQ消息返回结果）
        QueryResultVO resultVO = new QueryResultVO();
        resultVO.setQueryId(queryId);
        resultVO.setQuery(dto.getQuery());
        resultVO.setAnswer("这是一个模拟的回答，实际项目中应该由核心服务生成");
        resultVO.setExecutionTime(System.currentTimeMillis() - startTime);

        // 模拟文档片段
        List<QueryResultVO.DocumentFragmentVO> fragments = new ArrayList<>();
        QueryResultVO.DocumentFragmentVO fragment = new QueryResultVO.DocumentFragmentVO();
        fragment.setDocumentId(1L);
        fragment.setDocumentName("示例文档.pdf");
        fragment.setContent("这是文档中的一段内容，与查询相关");
        fragment.setSimilarity(0.95);
        fragment.setPageNumber(1);
        fragments.add(fragment);
        resultVO.setDocumentFragments(fragments);

        return resultVO;
    }

    @Override
    public List<KnowledgeBaseVO> getKnowledgeBaseList() {
        Long userId = UserContextUtils.getUserId();
        
        // 查询用户的知识库列表
        LambdaQueryWrapper<KnowledgeBaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBaseEntity::getUserId, userId)
                .orderByDesc(KnowledgeBaseEntity::getCreatedAt);

        List<KnowledgeBaseEntity> knowledgeBases = knowledgeBaseMapper.selectList(queryWrapper);

        // 转换为 VO
        List<KnowledgeBaseVO> knowledgeBaseVOs = new ArrayList<>();
        for (KnowledgeBaseEntity knowledgeBase : knowledgeBases) {
            KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
            BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);
            knowledgeBaseVOs.add(knowledgeBaseVO);
        }

        return knowledgeBaseVOs;
    }

    @Override
    public KnowledgeBaseVO getKnowledgeBaseDetail(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();
        
        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        // 转换为 VO
        KnowledgeBaseVO knowledgeBaseVO = new KnowledgeBaseVO();
        BeanUtils.copyProperties(knowledgeBase, knowledgeBaseVO);

        return knowledgeBaseVO;
    }

    @Override
    public List<DocumentVO> getDocumentList(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();
        
        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        // 查询文档列表
        LambdaQueryWrapper<DocumentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId)
                .orderByDesc(DocumentEntity::getCreatedAt);

        List<DocumentEntity> documents = documentMapper.selectList(queryWrapper);

        // 转换为 VO
        List<DocumentVO> documentVOs = new ArrayList<>();
        for (DocumentEntity document : documents) {
            DocumentVO documentVO = new DocumentVO();
            BeanUtils.copyProperties(document, documentVO);
            documentVOs.add(documentVO);
        }

        return documentVOs;
    }

    @Override
    @Transactional
    public boolean deleteKnowledgeBase(Long knowledgeBaseId) {
        Long userId = UserContextUtils.getUserId();
        
        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        // 删除相关文档
        LambdaQueryWrapper<DocumentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentEntity::getKnowledgeBaseId, knowledgeBaseId);
        documentMapper.delete(queryWrapper);

        // 删除知识库
        int result = knowledgeBaseMapper.deleteById(knowledgeBaseId);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteDocument(Long documentId) {
        Long userId = UserContextUtils.getUserId();
        
        // 校验文档是否存在
        DocumentEntity document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 校验知识库是否存在且用户有权限
        KnowledgeBaseEntity knowledgeBase = knowledgeBaseMapper.selectById(document.getKnowledgeBaseId());
        if (knowledgeBase == null || !knowledgeBase.getUserId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限");
        }

        // 删除文档
        int result = documentMapper.deleteById(documentId);
        
        // 更新知识库文档计数
        if (result > 0) {
            knowledgeBase.setDocumentCount(knowledgeBase.getDocumentCount() - 1);
            knowledgeBase.setUpdatedAt(LocalDateTime.now());
            knowledgeBaseMapper.updateById(knowledgeBase);
        }

        return result > 0;
    }
}
