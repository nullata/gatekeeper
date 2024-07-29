package com.gatekeeper.validators;

import com.gatekeeper.components.EnvironmentUtils;
import com.gatekeeper.components.SpringShutdownUtil;
import com.gatekeeper.events.ValidationCompleteEvent;
import com.gatekeeper.exceptions.EnvironmentValidationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class EnvironmentValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentValidator.class);
    private final SpringShutdownUtil shutdownUtil;
    private final EnvironmentUtils environmentUtils;
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

    @Value("${rate.limit.mode}")
    private String rateLimitMode;

    public EnvironmentValidator(SpringShutdownUtil shutdownUtil,
            EnvironmentUtils environmentUtils,
            ApplicationEventPublisher eventPublisher) {
        this.shutdownUtil = shutdownUtil;
        this.environmentUtils = environmentUtils;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Validating environment variables");
        try {
            ////////////////
            // REQUIRED
            ////////////////
            String proxyTarget = environmentUtils.validateEnvVar("PROXY_TARGET_URL");
            logger.info("PROXY_TARGET_URL set to: " + proxyTarget);

            String dbType = environmentUtils.validateEnvVar("DB_TYPE");
            logger.info("DB_TYPE:" + dbType);

            environmentUtils.validateEnvVar("DB_HOST");
            logger.info("DB_HOST validated");

            environmentUtils.validateEnvVar("DB_PORT");
            logger.info("DB_PORT validated");

            environmentUtils.validateEnvVar("DB_NAME");
            logger.info("DB_NAME validated");

            environmentUtils.validateEnvVar("DB_USERNAME");
            logger.info("DB_USERNAME validated");

            environmentUtils.validateEnvVar("DB_PASSWORD");
            logger.info("DB_PASSWORD validated");

            environmentUtils.validateEnvVar("TABLE_NAME");
            logger.info("TABLE_NAME validated");

            environmentUtils.validateEnvVar("COLUMN_NAME");
            logger.info("COLUMN_NAME validated");

            ////////////////
            // RATE LIMITER
            ////////////////
            Optional<String> rateLimitEnabled = environmentUtils.getEnvVar("RATE_LIMIT_ENABLED");
            if (rateLimitEnabled.isPresent()) {
                switch (rateLimitEnabled.get().toLowerCase()) {
                    case "true" -> {
                        logger.info("RATE_LIMIT_ENABLED: " + rateLimitEnabled.get());
                        Optional<String> rateLimit = environmentUtils.getEnvVar("RATE_LIMIT_RATE");
                        if (rateLimit.isPresent()) {
                            parseNumericValues("RATE_LIMIT_RATE", rateLimit.get());
                            logger.info("RATE_LIMIT_RATE: " + rateLimit.get());
                        } else {
                            logger.info("RATE_LIMIT_RATE: " + confRateLimitRate);
                        }

                        Optional<String> timeout = environmentUtils.getEnvVar("RATE_LIMIT_TIMEOUT");
                        if (timeout.isPresent()) {
                            parseNumericValues("RATE_LIMIT_TIMEOUT", timeout.get());
                            logger.info("RATE_LIMIT_TIMEOUT: " + timeout.get());
                        } else {
                            logger.info("RATE_LIMIT_TIMEOUT: " + confRateLimitTimeout);
                        }

                        Optional<String> mode = environmentUtils.getEnvVar("RATE_LIMIT_MODE");
                        if (mode.isPresent()) {
                            if (mode.get().equalsIgnoreCase("global")
                                    || mode.get().equalsIgnoreCase("individual")) {
                                logger.info("RATE_LIMIT_MODE: " + mode.get());
                            } else {
                                throw new EnvironmentValidationException("Unsupported value for RATE_LIMIT_MODE: " + mode.get());
                            }
                        } else {
                            logger.info("RATE_LIMIT_MODE: " + rateLimitMode);
                        }
                        break;
                    }
                    case "false" -> {
                        logger.info("RATE_LIMIT_ENABLED: " + rateLimitEnabled.get());
                        break;
                    }
                    default ->
                        throw new EnvironmentValidationException("Unsupported value for RATE_LIMIT_ENABLED: " + rateLimitEnabled.get());
                }
            } else {
                logger.info("RATE_LIMIT_ENABLED set to default: " + confRateLimitEnabled);
            }

            ////////////////
            // CACHING
            ////////////////
            Optional<String> cachingEnabled = environmentUtils.getEnvVar("ENABLE_CACHING");
            if (cachingEnabled.isPresent()) {
                switch (cachingEnabled.get().toLowerCase()) {
                    case "true" -> {
                        logger.info("ENABLE_CACHING: " + cachingEnabled.get());
                        if (cachingEnabled.get().equals("true")) {
                            Optional<String> cacheMaxSize = environmentUtils.getEnvVar("CACHE_MAX_SIZE");
                            if (cacheMaxSize.isPresent()) {
                                parseNumericValues("CACHE_MAX_SIZE", cacheMaxSize.get());
                                logger.info("CACHE_MAX_SIZE: " + cacheMaxSize.get());
                            } else {
                                logger.info("CACHE_MAX_SIZE set to default: " + confCacheMaxSize);
                            }

                            Optional<String> cacheMaxDur = environmentUtils.getEnvVar("CACHE_MAX_DURATION_M");
                            if (cacheMaxDur.isPresent()) {
                                parseNumericValues("CACHE_MAX_DURATION_M", cacheMaxDur.get());
                                logger.info("CACHE_MAX_DURATION_M: " + cacheMaxDur.get());
                            } else {
                                logger.info("CACHE_MAX_DURATION_M set to default: " + confCacheMaxDuration);
                            }
                        }
                        break;
                    }
                    case "false" -> {
                        logger.info("ENABLE_CACHING: " + cachingEnabled.get());
                        break;
                    }
                    default ->
                        throw new EnvironmentValidationException("Unsupported value for ENABLE_CACHING: " + cachingEnabled.get());
                }
            } else {
                logger.info("ENABLE_CACHING set to default: " + confEnableCaching);
                logger.info("CACHE_MAX_SIZE set to default: " + confCacheMaxSize);
                logger.info("CACHE_MAX_DURATION_M set to default: " + confCacheMaxDuration);
            }
        } catch (EnvironmentValidationException ex) {
            logger.error(ex.getMessage());
            shutdownUtil.shutDownSpringApp();
        }
        logger.info("Environment validation complete");
        eventPublisher.publishEvent(new ValidationCompleteEvent(this));
    }

    private void parseNumericValues(String envVarName, String value) throws EnvironmentValidationException {
        try {
            Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new EnvironmentValidationException("Provided value for " + envVarName + " is not numeric: " + value);
        }
    }
}
