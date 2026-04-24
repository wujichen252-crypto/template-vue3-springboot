package com.template.service.impl;

import com.template.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addToBlacklist(String token, long expirationMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        if (expirationMillis <= 0) {
            expirationMillis = 1;
        }
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
