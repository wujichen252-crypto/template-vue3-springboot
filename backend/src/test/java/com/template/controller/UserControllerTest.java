// file: backend/src/test/java/com/template/controller/UserControllerTest.java
package com.template.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.common.result.ApiResult;
import com.template.dto.LoginRequest;
import com.template.dto.RegisterRequest;
import com.template.service.TokenBlacklistService;
import com.template.service.UserService;
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("Password123");
        request.setEmail("newuser@example.com");

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@example.com")
                .status(1)
                .build();

        given(userService.register(any(RegisterRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
    }

    @Test
    void shouldReturn400WhenRegisterWithInvalidData() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // 太短
        request.setPassword("123"); // 不符合密码规则
        request.setEmail("invalid-email"); // 格式错误

        // when & then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        TokenResponse response = TokenResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .expiresIn(7200L)
                .build();

        given(userService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken123"))
                .andExpect(jsonPath("$.data.expiresIn").value(7200));
    }

    @Test
    void shouldReturn400WhenLoginWithInvalidData() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("ab"); // 太短
        request.setPassword("12345"); // 太短

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @WithMockUser(username = "1")
    void shouldGetCurrentUserSuccessfully() throws Exception {
        // given
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .status(1)
                .build();

        given(userService.getCurrentUser(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void shouldReturn401WhenGetCurrentUserWithoutAuth() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // given
        String refreshToken = "validRefreshToken";
        TokenResponse response = TokenResponse.builder()
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .expiresIn(7200L)
                .build();

        given(userService.refreshToken(refreshToken)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"));
    }

    @Test
    @WithMockUser(username = "1")
    void shouldLogoutSuccessfully() throws Exception {
        // given
        String accessToken = "validAccessToken";
        String authHeader = "Bearer " + accessToken;
        long expiration = 3600000L;

        given(userService.getAccessTokenExpiration(accessToken)).willReturn(expiration);

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tokenBlacklistService).addToBlacklist(accessToken, expiration);
    }

    @Test
    @WithMockUser(username = "1")
    void shouldLogoutSuccessfullyWithoutBearerPrefix() throws Exception {
        // given
        String authHeader = "InvalidTokenFormat";

        // when & then - 不会调用 blacklist，但不会报错
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn401WhenLogoutWithoutAuth() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
