package com.gatekeeper.validators;

import com.gatekeeper.components.EnvironmentUtils;
import com.gatekeeper.components.SpringShutdownUtil;
import com.gatekeeper.config.Constants;
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
        logger.info(Constants.MSG_VALIDATION_START);
        try {
            ////////////////
            // REQUIRED
            ////////////////
            String proxyTarget = environmentUtils.validateEnvVar(Constants.ENV_PROXY_TARGET_URL);
            logger.info(envVarPrintValue(Constants.ENV_PROXY_TARGET_URL, proxyTarget));

            String dbType = environmentUtils.validateEnvVar(Constants.ENV_DB_TYPE);
            logger.info(envVarPrintValue(Constants.ENV_DB_TYPE, dbType));

            environmentUtils.validateEnvVar(Constants.ENV_DB_HOST);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_HOST));

            environmentUtils.validateEnvVar(Constants.ENV_DB_PORT);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_PORT));

            environmentUtils.validateEnvVar(Constants.ENV_DB_NAME);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_NAME));

            environmentUtils.validateEnvVar(Constants.ENV_DB_USER);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_USER));

            environmentUtils.validateEnvVar(Constants.ENV_DB_PASS);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_PASS));

            environmentUtils.validateEnvVar(Constants.ENV_DB_TABLE);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_TABLE));

            environmentUtils.validateEnvVar(Constants.ENV_DB_COLUMN);
            logger.info(envVarValidatedMsg(Constants.ENV_DB_COLUMN));

            ////////////////
            // RATE LIMITER
            ////////////////
            Optional<String> rateLimitEnabled = environmentUtils.getEnvVar(Constants.ENV_RATE_LIMIT_ENABLED);
            if (rateLimitEnabled.isPresent()) {
                switch (rateLimitEnabled.get().toLowerCase()) {
                    case Constants.OPT_STR_TRUE -> {
                        logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_ENABLED, rateLimitEnabled.get()));
                        Optional<String> rateLimit = environmentUtils.getEnvVar(Constants.ENV_RATE_LIMIT_RATE);
                        if (rateLimit.isPresent()) {
                            parseNumericValues(Constants.ENV_RATE_LIMIT_RATE, rateLimit.get());
                            logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_RATE, rateLimit.get()));
                        } else {
                            logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_RATE,
                                    String.valueOf(confRateLimitRate)));
                        }

                        Optional<String> timeout = environmentUtils.getEnvVar(Constants.ENV_RATE_LIMIT_TIMEOUT);
                        if (timeout.isPresent()) {
                            parseNumericValues(Constants.ENV_RATE_LIMIT_TIMEOUT, timeout.get());
                            logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_TIMEOUT, timeout.get()));
                        } else {
                            logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_TIMEOUT,
                                    String.valueOf(confRateLimitTimeout)));
                        }

                        Optional<String> mode = environmentUtils.getEnvVar(Constants.ENV_RATE_LIMIT_MODE);
                        if (mode.isPresent()) {
                            if (mode.get().equalsIgnoreCase(Constants.OPT_RLM_GLOBAL)
                                    || mode.get().equalsIgnoreCase(Constants.OPT_RLM_INDI)) {
                                logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_MODE, mode.get()));
                            } else {
                                throw new EnvironmentValidationException(String.format(Constants.ERR_UNSUPPORTED_VALUE,
                                        Constants.ENV_RATE_LIMIT_MODE, mode.get()));
                            }
                        } else {
                            logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_MODE, rateLimitMode));
                        }
                        break;
                    }
                    case Constants.OPT_STR_FALSE -> {
                        logger.info(envVarPrintValue(Constants.ENV_RATE_LIMIT_ENABLED, rateLimitEnabled.get()));
                        break;
                    }
                    default ->
                        throw new EnvironmentValidationException(String.format(Constants.ERR_UNSUPPORTED_VALUE,
                                Constants.ENV_RATE_LIMIT_ENABLED, rateLimitEnabled.get()));
                }
            } else {
                logger.info(envVarDefaultVal(Constants.ENV_RATE_LIMIT_ENABLED, confRateLimitEnabled));
            }

            ////////////////
            // CACHING
            ////////////////
            Optional<String> cachingEnabled = environmentUtils.getEnvVar(Constants.ENV_ENABLE_CACHING);
            if (cachingEnabled.isPresent()) {
                switch (cachingEnabled.get().toLowerCase()) {
                    case Constants.OPT_STR_TRUE -> {
                        logger.info(envVarPrintValue(Constants.ENV_ENABLE_CACHING, cachingEnabled.get()));
                        if (cachingEnabled.get().equals(Constants.OPT_STR_TRUE)) {
                            Optional<String> cacheMaxSize = environmentUtils.getEnvVar(Constants.ENV_CACHE_MAX_SIZE);
                            if (cacheMaxSize.isPresent()) {
                                parseNumericValues(Constants.ENV_CACHE_MAX_SIZE, cacheMaxSize.get());
                                logger.info(envVarPrintValue(Constants.ENV_CACHE_MAX_SIZE, cacheMaxSize.get()));
                            } else {
                                logger.info(envVarDefaultVal(Constants.ENV_CACHE_MAX_SIZE,
                                        String.valueOf(confCacheMaxSize)));
                            }

                            Optional<String> cacheMaxDur = environmentUtils.getEnvVar(Constants.ENV_CACHE_MAX_DURATION);
                            if (cacheMaxDur.isPresent()) {
                                parseNumericValues(Constants.ENV_CACHE_MAX_DURATION, cacheMaxDur.get());
                                logger.info(envVarPrintValue(Constants.ENV_CACHE_MAX_DURATION, cacheMaxDur.get()));
                            } else {
                                logger.info(envVarDefaultVal(Constants.ENV_CACHE_MAX_DURATION,
                                        String.valueOf(confCacheMaxDuration)));
                            }
                        }
                        break;
                    }
                    case Constants.OPT_STR_FALSE -> {
                        logger.info(envVarPrintValue(Constants.ENV_ENABLE_CACHING, cachingEnabled.get()));
                        break;
                    }
                    default ->
                        throw new EnvironmentValidationException(String.format(Constants.ERR_UNSUPPORTED_VALUE,
                                Constants.ENV_ENABLE_CACHING, cachingEnabled.get()));
                }
            } else {
                logger.info(envVarDefaultVal(Constants.ENV_ENABLE_CACHING, confEnableCaching));
                logger.info(envVarDefaultVal(Constants.ENV_CACHE_MAX_SIZE, String.valueOf(confCacheMaxSize)));
                logger.info(envVarDefaultVal(Constants.ENV_CACHE_MAX_DURATION, String.valueOf(confCacheMaxDuration)));
            }
        } catch (EnvironmentValidationException ex) {
            logger.error(ex.getMessage());
            shutdownUtil.shutDownSpringApp();
        }
        logger.info(Constants.MSG_VALIDATION_END);
        eventPublisher.publishEvent(new ValidationCompleteEvent(this));
    }

    private void parseNumericValues(String envVarName, String value) throws EnvironmentValidationException {
        try {
            Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new EnvironmentValidationException(String.format(Constants.ERR_VALUE_NOT_NUMERIC, envVarName, value));
        }
    }

    private String envVarValidatedMsg(String varName) {
        return varName + " validated";
    }

    private String envVarPrintValue(String varName, String value) {
        return varName + ": " + value;
    }

    private String envVarDefaultVal(String varName, String value) {
        return envVarPrintValue(varName + " set to default", value);
    }
}
