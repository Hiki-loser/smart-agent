package com.smartagent.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.chat.entity.UserMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessageEntity> {
}

