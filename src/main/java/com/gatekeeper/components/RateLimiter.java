package com.gatekeeper.components;



import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class RateLimiter {

    @Value("${rate.limit.enabled}")
    private boolean rateLimitEnabled;

    @Value("${rate.limit.rate}")
    private int rateLimitRate;

    @Value("${rate.limit.timeout}")
    private int rateLimitTimeout;

    private Bucket bucket;

    @PostConstruct
    public void initializeBucket() {
        if (rateLimitEnabled) {
            Bandwidth limit = Bandwidth.classic(rateLimitRate, 
                    Refill.greedy(rateLimitRate, Duration.ofSeconds(rateLimitTimeout)));
            this.bucket = Bucket.builder().addLimit(limit).build();
        }
    }

    public boolean tryConsume() {
        return rateLimitEnabled && bucket.tryConsume(1);
    }
}
