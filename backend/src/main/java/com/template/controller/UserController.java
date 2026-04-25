package com.template.controller;

import com.template.annotation.CurrentUser;
import com.template.common.result.ApiResult;
import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.service.TokenBlacklistService;
import com.template.service.UserService;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/auth/register")
    public ApiResult<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ApiResult.success(user);
    }

    @PostMapping("/auth/login")
    public ApiResult<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse token = userService.login(request);
        return ApiResult.success(token);
    }

    @GetMapping("/users/me")
    public ApiResult<UserResponse> getCurrentUser(@CurrentUser Long userId) {
        UserResponse user = userService.getCurrentUser(userId);
        return ApiResult.success(user);
    }

    @PostMapping("/auth/refresh")
    public ApiResult<TokenResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
        TokenResponse token = userService.refreshToken(refreshToken);
        return ApiResult.success(token);
    }

    @PostMapping("/auth/logout")
    public ApiResult<Void> logout(@CurrentUser Long userId,
                                  @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expiration = userService.getAccessTokenExpiration(token);
            tokenBlacklistService.addToBlacklist(token, expiration);
        }
        return ApiResult.success();
    }
}
