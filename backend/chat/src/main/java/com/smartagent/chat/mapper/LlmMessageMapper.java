package com.smartagent.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.chat.entity.LlmMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LlmMessageMapper extends BaseMapper<LlmMessageEntity> {
}

