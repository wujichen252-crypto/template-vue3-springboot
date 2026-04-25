package com.template.service.impl;

import com.template.common.exception.BusinessException;
import com.template.common.result.ResultCode;
import com.template.common.util.JwtUtil;
import com.template.common.util.PasswordUtil;
import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.entity.User;
import com.template.mapper.UserMapper;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
    }

    @Test
    @DisplayName("登录成功")
    void login_Success() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordUtil.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refreshToken");
        when(jwtUtil.getAccessExpire()).thenReturn(7200000L);

        TokenResponse response = userService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getExpiresIn()).isEqualTo(7200000L);

        verify(userMapper).selectOne(any());
        verify(passwordUtil).matches("password123", "encodedPassword");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMsg());
                });
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordUtil.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getMsg());
                });
    }

    @Test
    @DisplayName("登录失败 - 用户被禁用")
    void login_UserDisabled() {
        // 创建被禁用的用户
        User disabledUser = new User();
        disabledUser.setId(1L);
        disabledUser.setUsername("testuser");
        disabledUser.setPasswordHash("encodedPassword");
        disabledUser.setEmail("test@example.com");
        disabledUser.setStatus(0);

        when(userMapper.selectOne(any())).thenReturn(disabledUser);
        // 用户被禁用时不会验证密码，所以不需要 stub passwordUtil.matches

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.FORBIDDEN.getMsg());
                });
    }

    @Test
    @DisplayName("注册成功")
    void register_Success() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordUtil.encode(anyString())).thenReturn("encodedPassword");

        UserResponse response = userService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("new@example.com");

        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 用户已存在")
    void register_UserExists() {
        when(userMapper.selectOne(any())).thenReturn(new User());

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USER_EXISTS.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.USER_EXISTS.getMsg());
                });

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("获取当前用户信息成功")
    void getCurrentUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        UserResponse response = userService.getCurrentUser(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("获取当前用户信息失败 - 用户不存在")
    void getCurrentUser_NotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getCurrentUser(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.NOT_FOUND.getMsg());
                });
    }

    @Test
    @DisplayName("刷新 Token 成功")
    void refreshToken_Success() {
        when(jwtUtil.getTypeFromToken(anyString())).thenReturn("refresh");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
        when(jwtUtil.generateAccessToken(any())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("newRefreshToken");
        when(jwtUtil.getAccessExpire()).thenReturn(7200000L);

        TokenResponse response = userService.refreshToken("validRefreshToken");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
    }

    @Test
    @DisplayName("刷新 Token 失败 - Token 类型错误")
    void refreshToken_WrongType() {
        when(jwtUtil.getTypeFromToken(anyString())).thenReturn("access");

        assertThatThrownBy(() -> userService.refreshToken("accessToken"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode());
                    assertThat(be.getMessage()).isEqualTo(ResultCode.UNAUTHORIZED.getMsg());
                });
    }

    @Test
    @DisplayName("获取 Token 过期时间")
    void getAccessTokenExpiration_Success() {
        Claims claims = mock(Claims.class);
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtUtil.parseToken(anyString())).thenReturn(claims);

        long expirationTime = userService.getAccessTokenExpiration("token");

        assertThat(expirationTime).isGreaterThan(0);
    }
}
