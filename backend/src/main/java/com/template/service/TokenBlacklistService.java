package com.template.service;

public interface TokenBlacklistService {

    void addToBlacklist(String token, long expirationMillis);

    boolean isBlacklisted(String token);
}
