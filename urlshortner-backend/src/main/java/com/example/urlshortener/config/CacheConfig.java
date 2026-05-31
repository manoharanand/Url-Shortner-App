package com.example.urlshortener.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager(ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryProvider.getIfAvailable();
        if (redisConnectionFactory != null) {
            try (var connection = redisConnectionFactory.getConnection()) {
                if ("PONG".equals(connection.ping())) {
                    RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                            .entryTtl(Duration.ofMinutes(30))
                            .disableCachingNullValues();

                    return RedisCacheManager.builder(redisConnectionFactory)
                            .cacheDefaults(cacheConfig)
                            .build();
                }
            } catch (Exception ex) {
                log.warn("Redis is not available, falling back to in-memory cache", ex);
            }
        } else {
            log.info("No RedisConnectionFactory available; using in-memory cache");
        }

        return new ConcurrentMapCacheManager();
    }
}
