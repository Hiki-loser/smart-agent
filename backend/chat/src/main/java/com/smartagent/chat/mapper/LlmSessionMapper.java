package com.smartagent.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.chat.entity.LlmSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LlmSessionMapper extends BaseMapper<LlmSessionEntity> {
}

