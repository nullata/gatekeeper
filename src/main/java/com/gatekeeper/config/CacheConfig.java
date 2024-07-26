package com.gatekeeper.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author null
 */
@Configuration
@ConditionalOnProperty(name = "enable.caching", havingValue = "true")
@EnableCaching
public class CacheConfig {
    
    @Value("${cache.max.size}")
    private int cacheMaxSize;
    
    @Value("${cache.max.duration}")
    private int cacheMaxDuration;

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
            .expireAfterWrite(cacheMaxSize, TimeUnit.MINUTES)
            .maximumSize(cacheMaxDuration);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
