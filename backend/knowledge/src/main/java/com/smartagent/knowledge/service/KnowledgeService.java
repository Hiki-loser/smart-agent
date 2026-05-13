package com.smartagent.knowledge.service;

import com.smartagent.knowledge.dto.CreateKnowledgeBaseDTO;
import com.smartagent.knowledge.dto.QueryDTO;
import com.smartagent.knowledge.vo.DocumentVO;
import com.smartagent.knowledge.vo.KnowledgeBaseVO;
import com.smartagent.knowledge.vo.QueryResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库服务接口
 * 定义知识库相关的核心业务逻辑
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public interface KnowledgeService {

    /**
     * 创建知识库
     *
     * @param dto 创建知识库参数
     * @return 知识库信息
     */
    KnowledgeBaseVO createKnowledgeBase(CreateKnowledgeBaseDTO dto);

    /**
     * 上传文档
     *
     * @param knowledgeBaseId 知识库ID
     * @param file 上传的文件
     * @return 文档信息
     */
    DocumentVO uploadDocument(Long knowledgeBaseId, MultipartFile file);

    /**
     * 查询知识库
     *
     * @param dto 查询参数
     * @return 查询结果
     */
    QueryResultVO queryKnowledgeBase(QueryDTO dto);

    /**
     * 获取知识库列表
     *
     * @return 知识库列表
     */
    List<KnowledgeBaseVO> getKnowledgeBaseList();

    /**
     * 获取知识库详情
     *
     * @param knowledgeBaseId 知识库ID
     * @return 知识库详情
     */
    KnowledgeBaseVO getKnowledgeBaseDetail(Long knowledgeBaseId);

    /**
     * 获取知识库文档列表
     *
     * @param knowledgeBaseId 知识库ID
     * @return 文档列表
     */
    List<DocumentVO> getDocumentList(Long knowledgeBaseId);

    /**
     * 删除知识库
     *
     * @param knowledgeBaseId 知识库ID
     * @return 是否删除成功
     */
    boolean deleteKnowledgeBase(Long knowledgeBaseId);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    boolean deleteDocument(Long documentId);
}
