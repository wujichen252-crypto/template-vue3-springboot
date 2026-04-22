package com.template.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.template.common.exception.BusinessException;
import com.template.common.result.ResultCode;
import com.template.common.util.JwtUtil;
import com.template.common.util.PasswordUtil;
import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.entity.User;
import com.template.mapper.UserMapper;
import com.template.service.UserService;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    @Override
    public TokenResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (!passwordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessExpire())
                .build();
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername())
                .or()
                .eq(User::getEmail, request.getEmail());
        Long count = userMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(1);

        userMapper.insert(user);

        return toUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = getUserById(userId);
        return toUserResponse(user);
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return user;
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        try {
            String type = jwtUtil.getTypeFromToken(refreshToken);
            if (!"refresh".equals(type)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtUtil.getAccessExpire())
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    @Override
    public long getAccessTokenExpiration(String token) {
        Claims claims = jwtUtil.parseToken(token);
        Date expiration = claims.getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
