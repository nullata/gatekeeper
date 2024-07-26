package com.gatekeeper.validators;

import com.gatekeeper.exceptions.EnvironmentValidationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class EnvironmentValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentValidator.class);
    private final Environment environment;
    private final ApplicationContext appContext;
    private final ApplicationEventPublisher eventPublisher;
    
    @Value("${enable.caching}")
    private String confEnableCaching;
    
    @Value("${cache.max.size}")
    private int confCacheMaxSize;
    
    @Value("${cache.max.duration}")
    private int confCacheMaxDuration;

    @Value("${rate.limit.enabled}")
    private String confRateLimitEnabled;

    @Value("${rate.limit.rate}")
    private int confRateLimitRate;

    @Value("${rate.limit.timeout}")
    private int confRateLimitTimeout;

    public EnvironmentValidator(Environment environment, ApplicationContext appContext,
            ApplicationEventPublisher eventPublisher) {
        this.environment = environment;
        this.appContext = appContext;
        this.eventPublisher = eventPublisher;
    }

    private Optional<String> getEnvVar(String envVarName) {
        return Optional.ofNullable(environment.getProperty(envVarName)).filter(s -> !s.isEmpty());
    }

    private String validateEnvVar(String envVarName) throws EnvironmentValidationException {
        return getEnvVar(envVarName)
                .orElseThrow(() -> new EnvironmentValidationException("Environment variable validation failed: " + envVarName));
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Validating environment variables");
        try {
            String proxyTarget = validateEnvVar("PROXY_TARGET_URL");
            logger.info("PROXY_TARGET_URL set to: " + proxyTarget);

            validateEnvVar("DB_HOST");
            logger.info("DB_HOST validated");

            validateEnvVar("DB_PORT");
            logger.info("DB_PORT validated");

            validateEnvVar("DB_NAME");
            logger.info("DB_NAME validated");

            validateEnvVar("DB_USERNAME");
            logger.info("DB_USERNAME validated");

            validateEnvVar("DB_PASSWORD");
            logger.info("DB_PASSWORD validated");

            Optional<String> rateLimitEnabled = getEnvVar("RATE_LIMIT_ENABLED");
            if (rateLimitEnabled.isPresent()) {
                logger.info("RATE_LIMIT_ENABLED: " + rateLimitEnabled.get());
                if (rateLimitEnabled.get().equals("true")) {
                    
                    Optional<String> rateLimit = getEnvVar("RATE_LIMIT_RATE");
                    if (rateLimit.isPresent()) {
                        logger.info("RATE_LIMIT_RATE: " + rateLimit.get());
                    } else {
                        logger.info("RATE_LIMIT_RATE: " + confRateLimitRate);
                    }
                    
                    Optional<String> timeout = getEnvVar("RATE_LIMIT_TIMEOUT");
                    if (timeout.isPresent()) {
                        logger.info("RATE_LIMIT_TIMEOUT: " + timeout.get());
                    } else {
                        logger.info("RATE_LIMIT_TIMEOUT: " + confRateLimitTimeout);
                    }
                    
                }
            } else {
                logger.info("RATE_LIMIT_ENABLED set to default: " + confRateLimitEnabled);
            }            

            Optional<String> cachingEnabled = getEnvVar("ENABLE_CACHING");
            if (cachingEnabled.isPresent()) {
                logger.info("ENABLE_CACHING: " + cachingEnabled.get());
                if (cachingEnabled.get().equals("true")) {
                    Optional<String> cacheMaxSize = getEnvVar("CACHE_MAX_SIZE");
                    if (cacheMaxSize.isPresent()) {
                        logger.info("CACHE_MAX_SIZE: " + cacheMaxSize.get());
                    } else {
                        logger.info("CACHE_MAX_SIZE set to default: " + confCacheMaxSize);
                    }
                    
                    Optional<String> cacheMaxDur = getEnvVar("CACHE_MAX_DURATION_M");
                    if (cacheMaxDur.isPresent()) {
                        logger.info("CACHE_MAX_DURATION_M: " + cacheMaxDur.get());
                    } else {
                        logger.info("CACHE_MAX_DURATION_M set to default: " + confCacheMaxDuration);
                    }
                }
            } else {
                logger.info("ENABLE_CACHING set to default: " + confEnableCaching);
                logger.info("CACHE_MAX_SIZE set to default: " + confCacheMaxSize);
                logger.info("CACHE_MAX_DURATION_M set to default: " + confCacheMaxDuration);
            }
            

            
        } catch (EnvironmentValidationException ex) {
            logger.error(ex.getMessage());
            SpringApplication.exit(appContext, () -> 1);
            return;
        }
        logger.info("Environment validation complete");
        eventPublisher.publishEvent(new ValidationCompleteEvent(this));
    }
}
