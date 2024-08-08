package com.gatekeeper.components;

import com.gatekeeper.config.Constants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class RateLimiter {

    @Value("${rate.limit.rate}")
    private int rateLimitRate;

    @Value("${rate.limit.timeout}")
    private int rateLimitTimeout;
    
    @Value("${rate.limit.mode}")
    private String rateLimitMode;

    private Bucket globalBucket;
    private final ConcurrentMap<String, Bucket> keyBuckets = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeBucket() {
        this.globalBucket = createNewBucket();
    }
    
    public boolean tryConsume(String apiKey) {
        if (rateLimitMode.equalsIgnoreCase(Constants.OPT_RLM_GLOBAL)) {
            return globalBucket.tryConsume(1);
        } else if (rateLimitMode.equalsIgnoreCase(Constants.OPT_RLM_INDI)) {
            return keyBuckets.computeIfAbsent(apiKey, k -> createNewBucket()).tryConsume(1);
        }
        return false;
    }
    
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(rateLimitRate, 
                Refill.greedy(rateLimitRate, Duration.ofSeconds(rateLimitTimeout)));
        return Bucket.builder().addLimit(limit).build();
    }
}
