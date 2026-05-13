package com.smartagent.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.user.domain.entity.ApiKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * API Key Mapper
 * 用于API Key的数据库操作
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Mapper
public interface ApiKeyMapper extends BaseMapper<ApiKey> {

}
