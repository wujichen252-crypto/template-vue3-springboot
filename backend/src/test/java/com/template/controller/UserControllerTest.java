package com.template.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.common.util.JwtUtil;
import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.interceptor.JwtAuthenticationFilter;
import com.template.mapper.UserMapper;
import com.template.service.TokenBlacklistService;
import com.template.service.UserService;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.template.config.MyBatisPlusConfig.class,
                        com.template.config.RedisConfig.class,
                        com.template.config.CacheConfig.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("用户控制器单元测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserMapper userMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private TokenResponse tokenResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123");
        registerRequest.setEmail("new@example.com");

        tokenResponse = TokenResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .expiresIn(7200000L)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("用户注册接口测试")
    void register_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("用户登录接口测试")
    void login_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken123"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("获取当前用户信息接口测试")
    @WithMockUser(username = "1")
    void getCurrentUser_Success() throws Exception {
        when(userService.getCurrentUser(anyLong())).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getCurrentUser(anyLong());
    }

    @Test
    @DisplayName("刷新 Token 接口测试")
    void refreshToken_Success() throws Exception {
        when(userService.refreshToken(anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .header("X-Refresh-Token", "validRefreshToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken123"));

        verify(userService).refreshToken("validRefreshToken");
    }

    @Test
    @DisplayName("用户登出接口测试")
    @WithMockUser(username = "1")
    void logout_Success() throws Exception {
        when(userService.getAccessTokenExpiration(anyString())).thenReturn(3600000L);
        doNothing().when(tokenBlacklistService).addToBlacklist(anyString(), anyLong());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tokenBlacklistService).addToBlacklist(eq("validToken"), anyLong());
    }

    @Test
    @DisplayName("注册接口参数校验 - 用户名不能为空")
    void register_ValidationError() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("Password123");
        invalidRequest.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
