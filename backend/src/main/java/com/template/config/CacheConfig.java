package com.template.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.data.redis.host")
public class CacheConfig {

    @Value("${app.cache.user-ttl:30}")
    private Long userTtl;

    @Value("${app.cache.user-list-ttl:5}")
    private Long userListTtl;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("user", RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(userTtl)))
                .withCacheConfiguration("userList", RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(userListTtl)))
                .build();
    }
}
