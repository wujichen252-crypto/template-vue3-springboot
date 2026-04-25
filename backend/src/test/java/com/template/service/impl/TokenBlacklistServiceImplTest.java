package com.template.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Token 黑名单服务单元测试")
class TokenBlacklistServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        // 只在需要时设置 stubbing
    }

    @Test
    @DisplayName("添加 Token 到黑名单成功")
    void addToBlacklist_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        String token = "testToken";
        long expiration = 3600000L;

        tokenBlacklistService.addToBlacklist(token, expiration);

        verify(valueOperations).set(
                eq("token:blacklist:testToken"),
                eq("1"),
                eq(expiration),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("添加 Token 到黑名单 - Token 为空不执行")
    void addToBlacklist_EmptyToken() {
        tokenBlacklistService.addToBlacklist(null, 3600000L);
        tokenBlacklistService.addToBlacklist("", 3600000L);

        verify(redisTemplate, never()).opsForValue();
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("添加 Token 到黑名单 - 过期时间小于等于0时设为1ms")
    void addToBlacklist_InvalidExpiration() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        String token = "testToken";

        tokenBlacklistService.addToBlacklist(token, 0);

        verify(valueOperations).set(
                eq("token:blacklist:testToken"),
                eq("1"),
                eq(1L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("检查 Token 在黑名单中")
    void isBlacklisted_True() {
        String token = "blacklistedToken";
        when(redisTemplate.hasKey("token:blacklist:blacklistedToken")).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查 Token 不在黑名单中")
    void isBlacklisted_False() {
        String token = "validToken";
        when(redisTemplate.hasKey("token:blacklist:validToken")).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("检查 Token - 空 Token 返回 false")
    void isBlacklisted_NullToken() {
        assertThat(tokenBlacklistService.isBlacklisted(null)).isFalse();
        assertThat(tokenBlacklistService.isBlacklisted("")).isFalse();

        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("检查 Token - Redis 返回 null 时返回 false")
    void isBlacklisted_RedisNull() {
        String token = "testToken";
        when(redisTemplate.hasKey("token:blacklist:testToken")).thenReturn(null);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }
}
