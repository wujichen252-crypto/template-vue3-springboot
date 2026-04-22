package com.template.service;

import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.entity.User;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;

public interface UserService {

    TokenResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse getCurrentUser(Long userId);

    User getUserById(Long userId);

    TokenResponse refreshToken(String refreshToken);

    long getAccessTokenExpiration(String token);
}
