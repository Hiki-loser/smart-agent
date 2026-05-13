package com.smartagent.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.knowledge.entity.DocumentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档Mapper
 * 继承BaseMapper，使用MyBatis-Plus方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {
}
