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
        logger.trace("Validating environment variables");
        try {
            String proxyTarget = validateEnvVar("PROXY_TARGET_URL");
            logger.trace("PROXY_TARGET_URL set to: " + proxyTarget);
            
            String dbHost = validateEnvVar("DB_HOST");            
            logger.trace("DB_HOST validated");
            
            String dbPort = validateEnvVar("DB_PORT");
            logger.trace("DB_PORT validated");
            
            String dbName = validateEnvVar("DB_NAME");
            logger.trace("DB_NAME validated");
            
            String dbUser = validateEnvVar("DB_USERNAME");
            logger.trace("DB_USERNAME validated");
            
            String dbPass = validateEnvVar("DB_PASSWORD");
            logger.trace("DB_PASSWORD validated");
            
        } catch (EnvironmentValidationException ex) {
            logger.error(ex.getMessage());
            SpringApplication.exit(appContext, () -> 1);
            return;
        }
        logger.trace("Environment validation complete");
        eventPublisher.publishEvent(new ValidationCompleteEvent(this));
    }
}
