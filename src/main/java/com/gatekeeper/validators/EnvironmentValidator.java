package com.gatekeeper.validators;

import com.gatekeeper.exceptions.EnvironmentValidationException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public EnvironmentValidator(Environment environment, ApplicationContext appContext, 
            ApplicationEventPublisher eventPublisher) {
        this.environment = environment;
        this.appContext = appContext;
        this.eventPublisher = eventPublisher;
    }

    private String validateEnvVar(String envVarName) throws EnvironmentValidationException {
        return Optional.ofNullable(environment.getProperty(envVarName))
                .filter(s -> !s.isEmpty())
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
            
            String rateLimitEnabled = validateEnvVar("RATE_LIMIT_ENABLED");
            logger.info("RATE_LIMIT_ENABLED: " + rateLimitEnabled);
            
            if (rateLimitEnabled.equals("true")) {
                String rateLimit = validateEnvVar("RATE_LIMIT_RATE");
                logger.info("RATE_LIMIT_RATE: " + rateLimit);
                
                String timeout = validateEnvVar("RATE_LIMIT_TIMEOUT");
                logger.info("RATE_LIMIT_TIMEOUT: " + timeout);
            }
            
            String cachingEnabled = validateEnvVar("ENABLE_CACHING");
            logger.info("ENABLE_CACHING: " + cachingEnabled);
            
            if (cachingEnabled.equals("true")) {
                String cacheMaxSize = validateEnvVar("CACHE_MAX_SIZE");
                logger.info("CACHE_MAX_SIZE: " + cacheMaxSize);
            
                String cacheMaxDur = validateEnvVar("CACHE_MAX_DURATION_M");
                logger.info("CACHE_MAX_DURATION_M: " + cacheMaxDur);
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
