package com.smartagent.user.controller;

import com.smartagent.common.model.ApiResponse;
import com.smartagent.user.domain.dto.UserLoginDTO;
import com.smartagent.user.domain.dto.UserRegisterDTO;
import com.smartagent.user.domain.dto.UserUpdateDTO;
import com.smartagent.user.domain.vo.ApiKeyVO;
import com.smartagent.user.domain.vo.LoginVO;
import com.smartagent.user.domain.vo.UserVO;
import com.smartagent.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 实现用户相关的HTTP接口
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return ApiResponse.success(userVO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return ApiResponse.success(loginVO);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return ApiResponse.success(userVO);
    }

    /**
     * 更新用户信息
     *
     * @param updateDTO 更新参数
     * @return 更新后的用户信息
     */
    @PutMapping("/update")
    public ApiResponse<UserVO> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO) {
        UserVO userVO = userService.updateUser(updateDTO);
        return ApiResponse.success(userVO);
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        userService.logout();
        return ApiResponse.success();
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public ApiResponse<String> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        String newAccessToken = userService.refreshToken(refreshToken);
        return ApiResponse.success(newAccessToken);
    }

    /**
     * 创建API Key
     *
     * @param name API Key名称
     * @param expireDays 过期天数，null表示永不过期
     * @return API Key信息
     */
    @PostMapping("/api-key")
    public ApiResponse<ApiKeyVO> createApiKey(@RequestParam("name") String name, @RequestParam(required = false) Integer expireDays) {
        ApiKeyVO apiKeyVO = userService.createApiKey(name, expireDays);
        return ApiResponse.success(apiKeyVO);
    }

    /**
     * 获取API Key列表
     *
     * @return API Key列表
     */
    @GetMapping("/api-key/list")
    public ApiResponse<java.util.List<ApiKeyVO>> getApiKeyList() {
        java.util.List<ApiKeyVO> apiKeyVOs = userService.getApiKeyList();
        return ApiResponse.success(apiKeyVOs);
    }

    /**
     * 吊销API Key
     *
     * @param apiKeyId API Key ID
     * @return 操作结果
     */
    @DeleteMapping("/api-key/{apiKeyId}")
    public ApiResponse<Void> revokeApiKey(@PathVariable("apiKeyId") Long apiKeyId) {
        userService.revokeApiKey(apiKeyId);
        return ApiResponse.success();
    }
}
