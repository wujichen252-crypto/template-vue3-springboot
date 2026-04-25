package com.template.service.impl;

import com.template.service.TokenBlacklistService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "spring.data.redis.host", matchIfMissing = true)
public class InMemoryTokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token, long expirationMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        long expireAt = System.currentTimeMillis() + Math.max(expirationMillis, 1);
        blacklist.put(token, expireAt);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long expireAt = blacklist.get(token);
        if (expireAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireAt) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
