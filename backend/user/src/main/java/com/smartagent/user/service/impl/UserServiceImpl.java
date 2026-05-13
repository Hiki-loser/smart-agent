package com.smartagent.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.common.constant.UserConstant;
import com.smartagent.common.exception.BizException;
import com.smartagent.common.enums.ResultCode;
import com.smartagent.user.domain.dto.UserLoginDTO;
import com.smartagent.user.domain.dto.UserRegisterDTO;
import com.smartagent.user.domain.dto.UserUpdateDTO;
import com.smartagent.user.domain.entity.ApiKey;
import com.smartagent.user.domain.entity.User;
import com.smartagent.user.domain.entity.UserLoginLog;
import com.smartagent.user.domain.entity.UserToken;
import com.smartagent.user.domain.vo.ApiKeyVO;
import com.smartagent.user.domain.vo.LoginVO;
import com.smartagent.user.domain.vo.UserVO;
import com.smartagent.user.mapper.ApiKeyMapper;
import com.smartagent.user.mapper.UserLoginLogMapper;
import com.smartagent.user.mapper.UserMapper;
import com.smartagent.user.mapper.UserTokenMapper;
import com.smartagent.user.security.JwtUtil;
import com.smartagent.user.service.UserService;
import com.smartagent.user.util.PasswordUtil;
import com.smartagent.user.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * 实现用户相关的核心业务逻辑
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final ApiKeyMapper apiKeyMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;


    public UserServiceImpl(UserMapper userMapper,
                           UserTokenMapper userTokenMapper,
                           UserLoginLogMapper userLoginLogMapper,
                           ApiKeyMapper apiKeyMapper,
                           RedisTemplate<String, String> redisTemplate,
                           HttpServletRequest request,JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.userTokenMapper = userTokenMapper;
        this.userLoginLogMapper = userLoginLogMapper;
        this.apiKeyMapper = apiKeyMapper;
        this.redisTemplate = redisTemplate;
        this.request = request;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO registerDTO) {
        // 校验用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.exists(queryWrapper)) {
            throw new BizException(ResultCode.PARAM_ERROR, "用户名已存在");
        }

        // 密码加密
        String encryptedPassword = PasswordUtil.encode(registerDTO.getPassword());

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(encryptedPassword);
        user.setRoleId(2L); // 默认普通用户角色
        user.setNickname(registerDTO.getNickname());
        user.setStatus(1); // 正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }

    @Override
    @Transactional
    public LoginVO login(UserLoginDTO loginDTO) {
        // 查找用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            saveLoginLog(loginDTO.getUsername(), 0, "用户不存在");
            throw new BizException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }

        // 校验密码
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            saveLoginLog(loginDTO.getUsername(), 0, "密码错误");
            throw new BizException(ResultCode.PARAM_ERROR, "用户名或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() != 1) {
            saveLoginLog(loginDTO.getUsername(), 0, "用户被禁用");
            throw new BizException(ResultCode.FORBIDDEN, "用户被禁用");
        }
        UserContext.setUserId(user.getId());
        // 生成令牌
        String deviceType = loginDTO.getDeviceType() != null ? loginDTO.getDeviceType() : "web";
        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 存储到Redis
        String accessTokenKey = String.format(UserConstant.USER_TOKEN_KEY, user.getId(), deviceType);
        String refreshTokenKey = String.format(UserConstant.USER_REFRESH_KEY, user.getId(), deviceType);
        redisTemplate.opsForValue().set(accessTokenKey, accessToken, UserConstant.ACCESS_TOKEN_EXPIRE, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, UserConstant.REFRESH_TOKEN_EXPIRE, TimeUnit.DAYS);

        // 存储到数据库
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setAccessToken(accessToken);
        userToken.setRefreshToken(refreshToken);
        userToken.setDeviceType(deviceType);
        userToken.setExpireTime(LocalDateTime.now().plusHours(UserConstant.ACCESS_TOKEN_EXPIRE));
        userToken.setCreateTime(LocalDateTime.now());
        userTokenMapper.insert(userToken);

        // 保存登录日志
        saveLoginLog(user.getUsername(), 1, "登录成功");

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setTokenType(UserConstant.TOKEN_PREFIX);
        loginVO.setExpiresIn(UserConstant.ACCESS_TOKEN_EXPIRE * 3600);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        loginVO.setUser(userVO);

        return loginVO;
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    @Transactional
    public UserVO updateUser(UserUpdateDTO updateDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // 更新用户信息
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void logout() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return;
        }

        // 1.从请求头获取令牌
        String token = request.getHeader(UserConstant.TOKEN_HEADER);
        if (token != null && token.startsWith(UserConstant.TOKEN_PREFIX)) {
            token = token.substring(7);
        }

        // 2. 获取设备类型（关键点）
        String deviceType = request.getHeader(UserConstant.DEVICE_TYPE_HEADER);
        if (deviceType == null) {
            deviceType = UserConstant.DEVICE_TYPE_WEB; // 默认
        }

        // 清除Redis中的令牌
        String accessTokenKey = String.format(UserConstant.USER_TOKEN_KEY, userId, deviceType);
        String refreshTokenKey = String.format(UserConstant.USER_REFRESH_KEY, userId, deviceType);
        redisTemplate.delete(accessTokenKey);
        redisTemplate.delete(refreshTokenKey);

        // 清除上下文
        UserContext.clear();
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 解析刷新令牌
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "无效的刷新令牌");
        }

        // 验证刷新令牌是否存在于Redis
        String deviceType = "web"; // 简化处理，实际应该从请求中获取
        String refreshTokenKey = String.format(UserConstant.USER_REFRESH_KEY, userId, deviceType);
        String storedRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BizException(ResultCode.UNAUTHORIZED, "无效的刷新令牌");
        }

        // 生成新的访问令牌
        String newAccessToken = jwtUtil.generateToken(userId);

        // 更新Redis
        String accessTokenKey = String.format(UserConstant.USER_TOKEN_KEY, userId, deviceType);
        redisTemplate.opsForValue().set(accessTokenKey, newAccessToken, UserConstant.ACCESS_TOKEN_EXPIRE, TimeUnit.HOURS);

        return newAccessToken;
    }

    /**
     * 保存登录日志
     */
    private void saveLoginLog(String username, int status, String message) {
        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUserId(UserContext.getUserId());
        loginLog.setUsername(username);
        loginLog.setIp(request.getRemoteAddr());
        loginLog.setDevice(request.getHeader("User-Agent"));
        loginLog.setStatus(status);
        loginLog.setMessage(message);
        loginLog.setLoginTime(LocalDateTime.now());
        userLoginLogMapper.insert(loginLog);
    }

    @Override
    @Transactional
    public ApiKeyVO createApiKey(String name, Integer expireDays) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }

        // 生成UUID作为API Key
        String keyValue = java.util.UUID.randomUUID().toString();

        // 创建API Key实体
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setKeyValue(keyValue);
        apiKey.setName(name);
        apiKey.setStatus(1); // 1=有效
        
        // 设置过期时间
        if (expireDays != null) {
            apiKey.setExpireAt(LocalDateTime.now().plusDays(expireDays));
        }
        
        apiKey.setCreateTime(LocalDateTime.now());
        apiKey.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        apiKeyMapper.insert(apiKey);

        // 转换为VO
        ApiKeyVO apiKeyVO = new ApiKeyVO();
        BeanUtils.copyProperties(apiKey, apiKeyVO);

        return apiKeyVO;
    }

    @Override
    public List<ApiKeyVO> getApiKeyList() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }

        // 查询用户的所有API Key
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ApiKey> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(ApiKey::getUserId, userId);
        queryWrapper.orderByDesc(ApiKey::getCreateTime);

        java.util.List<ApiKey> apiKeys = apiKeyMapper.selectList(queryWrapper);

        // 转换为VO列表
        java.util.List<ApiKeyVO> apiKeyVOs = new java.util.ArrayList<>();
        for (ApiKey apiKey : apiKeys) {
            ApiKeyVO apiKeyVO = new ApiKeyVO();
            BeanUtils.copyProperties(apiKey, apiKeyVO);
            apiKeyVOs.add(apiKeyVO);
        }

        return apiKeyVOs;
    }

    @Override
    @Transactional
    public void revokeApiKey(Long apiKeyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录");
        }

        // 查询API Key
        ApiKey apiKey = apiKeyMapper.selectById(apiKeyId);
        if (apiKey == null) {
            throw new BizException(ResultCode.NOT_FOUND, "API Key不存在");
        }

        // 校验所有权
        if (!apiKey.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作此API Key");
        }

        // 吊销API Key
        apiKey.setStatus(0); // 0=已吊销
        apiKey.setUpdateTime(LocalDateTime.now());
        apiKeyMapper.updateById(apiKey);
    }

}
