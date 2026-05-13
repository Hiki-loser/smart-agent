package com.smartagent.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.user.domain.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 * 继承BaseMapper，使用MyBatis-Plus方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}
