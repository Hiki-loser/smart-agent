package com.smartagent.knowledge.controller;

import com.smartagent.common.enums.ResultCode;
import com.smartagent.knowledge.dto.CreateKnowledgeBaseDTO;
import com.smartagent.knowledge.dto.QueryDTO;
import com.smartagent.knowledge.service.KnowledgeService;
import com.smartagent.knowledge.vo.DocumentVO;
import com.smartagent.knowledge.vo.KnowledgeBaseVO;
import com.smartagent.knowledge.vo.QueryResultVO;
import com.smartagent.common.model.ApiResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * 处理知识库相关的 HTTP 请求
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    /**
     * 创建知识库
     *
     * @param dto 创建知识库参数
     * @return 知识库信息
     */
    @PostMapping("/bases")
    public ApiResponse<KnowledgeBaseVO> createKnowledgeBase(@RequestBody CreateKnowledgeBaseDTO dto) {
        try {
            KnowledgeBaseVO knowledgeBaseVO = knowledgeService.createKnowledgeBase(dto);
            return ApiResponse.success(knowledgeBaseVO);
        } catch (Exception e) {
            log.error("Failed to create knowledge base: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 上传文档
     *
     * @param knowledgeBaseId 知识库ID
     * @param file 上传的文件
     * @return 文档信息
     */
    @PostMapping("/bases/{knowledgeBaseId}/documents")
    public ApiResponse<DocumentVO> uploadDocument(@PathVariable("knowledgeBaseId") Long knowledgeBaseId, @RequestParam("file") MultipartFile file) {
        try {
            DocumentVO documentVO = knowledgeService.uploadDocument(knowledgeBaseId, file);
            return ApiResponse.success(documentVO);
        } catch (Exception e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.SERVER_ERROR);
        }
    }

    /**
     * 查询知识库
     *
     * @param dto 查询参数
     * @return 查询结果
     */
    @PostMapping("/query")
    public ApiResponse<QueryResultVO> queryKnowledgeBase(@RequestBody QueryDTO dto) {
        try {
            QueryResultVO queryResultVO = knowledgeService.queryKnowledgeBase(dto);
            return ApiResponse.success(queryResultVO);
        } catch (Exception e) {
            log.error("Failed to query knowledge base: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.NOT_FOUND);
        }
    }

    /**
     * 获取知识库列表
     *
     * @return 知识库列表
     */
    @GetMapping("/bases")
    public ApiResponse<List<KnowledgeBaseVO>> getKnowledgeBaseList() {
        try {
            List<KnowledgeBaseVO> knowledgeBaseVOs = knowledgeService.getKnowledgeBaseList();
            return ApiResponse.success(knowledgeBaseVOs);
        } catch (Exception e) {
            log.error("Failed to get knowledge base list: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.NOT_FOUND);
        }
    }

    /**
     * 获取知识库详情
     *
     * @param knowledgeBaseId 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/bases/{knowledgeBaseId}")
    public ApiResponse<KnowledgeBaseVO> getKnowledgeBaseDetail(@PathVariable("knowledgeBaseId") Long knowledgeBaseId) {
        try {
            KnowledgeBaseVO knowledgeBaseVO = knowledgeService.getKnowledgeBaseDetail(knowledgeBaseId);
            return ApiResponse.success(knowledgeBaseVO);
        } catch (Exception e) {
            log.error("Failed to get knowledge base detail: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 获取知识库文档列表
     *
     * @param knowledgeBaseId 知识库ID
     * @return 文档列表
     */
    @GetMapping("/bases/{knowledgeBaseId}/documents")
    public ApiResponse<List<DocumentVO>> getDocumentList(@PathVariable("knowledgeBaseId") Long knowledgeBaseId) {
        try {
            List<DocumentVO> documentVOs = knowledgeService.getDocumentList(knowledgeBaseId);
            return ApiResponse.success(documentVOs);
        } catch (Exception e) {
            log.error("Failed to get document list: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.NOT_FOUND);
        }
    }

    /**
     * 删除知识库
     *
     * @param knowledgeBaseId 知识库ID
     * @return 是否删除成功
     */
    @DeleteMapping("/bases/{knowledgeBaseId}")
    public ApiResponse<Boolean> deleteKnowledgeBase(@PathVariable("knowledgeBaseId") Long knowledgeBaseId) {
        try {
            boolean result = knowledgeService.deleteKnowledgeBase(knowledgeBaseId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Failed to delete knowledge base: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Boolean> deleteDocument(@PathVariable("documentId") Long documentId) {
        try {
            boolean result = knowledgeService.deleteDocument(documentId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Failed to delete document: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.SERVER_ERROR);
        }
    }

    /**
     * 健康检查 - 返回向量存储后端状态
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "smart-agent-knowledge");
        health.put("status", "UP");
        health.put("vectorStore", knowledgeService.getVectorStoreInfo());
        return ApiResponse.success(health);
    }
}
