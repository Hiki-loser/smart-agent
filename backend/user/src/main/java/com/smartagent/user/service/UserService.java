package com.smartagent.user.service;

import com.smartagent.user.domain.dto.UserLoginDTO;
import com.smartagent.user.domain.dto.UserRegisterDTO;
import com.smartagent.user.domain.dto.UserUpdateDTO;
import com.smartagent.user.domain.vo.ApiKeyVO;
import com.smartagent.user.domain.vo.LoginVO;
import com.smartagent.user.domain.vo.UserVO;

/**
 * 用户服务接口
 * 定义用户相关的核心业务逻辑
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO login(UserLoginDTO loginDTO);

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新参数
     * @return 更新后的用户信息
     */
    UserVO updateUser(UserUpdateDTO updateDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);

    /**
     * 创建API Key
     *
     * @param name API Key名称
     * @param expireDays 过期天数，null表示永不过期
     * @return API Key信息
     */
    ApiKeyVO createApiKey(String name, Integer expireDays);

    /**
     * 获取用户的API Key列表
     *
     * @return API Key列表
     */
    java.util.List<ApiKeyVO> getApiKeyList();

    /**
     * 吊销API Key
     *
     * @param apiKeyId API Key ID
     */
    void revokeApiKey(Long apiKeyId);

}
