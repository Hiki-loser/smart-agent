package com.smartagent.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.chat.entity.SessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话Mapper
 * 继承BaseMapper，使用MyBatis-Plus方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Mapper
public interface SessionMapper extends BaseMapper<SessionEntity> {
}
