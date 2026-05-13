package com.smartagent.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartagent.user.domain.entity.UserToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Token Mapper
 * 继承BaseMapper，使用MyBatis-Plus方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
}
