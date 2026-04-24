// file: backend/src/test/java/com/template/service/impl/UserServiceImplTest.java
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
import com.template.vo.TokenResponse;
import com.template.vo.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldLoginSuccessfully() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");
        user.setStatus(1);

        given(userMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(user);
        given(passwordUtil.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtUtil.generateAccessToken(1L)).willReturn("accessToken123");
        given(jwtUtil.generateRefreshToken(1L)).willReturn("refreshToken123");
        given(jwtUtil.getAccessExpire()).willReturn(7200L);

        // when
        TokenResponse response = userService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getExpiresIn()).isEqualTo(7200L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        given(userMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode());
                });
    }

    @Test
    void shouldThrowExceptionWhenPasswordMismatch() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");
        user.setStatus(1);

        given(userMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(user);
        given(passwordUtil.matches("wrongpassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode());
                });
    }

    @Test
    void shouldThrowExceptionWhenUserDisabled() {
        // given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");
        user.setStatus(0);

        given(userMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(user);
        // 注意：密码验证在状态检查之后，所以这里不需要 stub passwordUtil.matches

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.FORBIDDEN.getCode());
                });
    }

    @Test
    void shouldRegisterSuccessfully() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("newuser@example.com");

        given(userMapper.selectCount(any(LambdaQueryWrapper.class))).willReturn(0L);
        given(passwordUtil.encode("password123")).willReturn("encodedPassword");
        given(userMapper.insert(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });

        // when
        UserResponse response = userService.register(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameOrEmailExists() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setEmail("existing@example.com");

        given(userMapper.selectCount(any(LambdaQueryWrapper.class))).willReturn(1L);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.USER_EXISTS.getCode());
                });

        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setStatus(1);

        given(userMapper.selectById(1L)).willReturn(user);

        // when
        UserResponse response = userService.getCurrentUser(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        given(userMapper.selectById(1L)).willReturn(user);

        // when
        User result = userService.getUserById(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        // given
        given(userMapper.selectById(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
                });
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // given
        String refreshToken = "validRefreshToken";
        given(jwtUtil.getTypeFromToken(refreshToken)).willReturn("refresh");
        given(jwtUtil.getUserIdFromToken(refreshToken)).willReturn(1L);
        given(jwtUtil.generateAccessToken(1L)).willReturn("newAccessToken");
        given(jwtUtil.generateRefreshToken(1L)).willReturn("newRefreshToken");
        given(jwtUtil.getAccessExpire()).willReturn(7200L);

        // when
        TokenResponse response = userService.refreshToken(refreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        assertThat(response.getExpiresIn()).isEqualTo(7200L);
    }

    @Test
    void shouldThrowExceptionWhenTokenTypeIsNotRefresh() {
        // given
        String accessToken = "validAccessToken";
        given(jwtUtil.getTypeFromToken(accessToken)).willReturn("access");

        // when & then
        assertThatThrownBy(() -> userService.refreshToken(accessToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(ResultCode.UNAUTHORIZED.getCode());
                });
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        // given
        String invalidToken = "invalidToken";
        given(jwtUtil.getTypeFromToken(invalidToken)).willThrow(new RuntimeException("Invalid token"));

        // when & then - 现在异常会直接抛出，不再包装
        assertThatThrownBy(() -> userService.refreshToken(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token");
    }
}
