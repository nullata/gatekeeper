package com.gatekeeper.components;

import com.gatekeeper.exceptions.EnvironmentValidationException;
import java.util.Optional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class EnvironmentUtils {

    private final Environment environment;

    public EnvironmentUtils(Environment environment) {
        this.environment = environment;
    }

    public Optional<String> getEnvVar(String envVarName) {
        return Optional.ofNullable(environment.getProperty(envVarName)).filter(s -> !s.isEmpty());
    }

    public String validateEnvVar(String envVarName) throws EnvironmentValidationException {
        return getEnvVar(envVarName)
                .orElseThrow(() -> new EnvironmentValidationException("Environment variable validation failed: " + envVarName));
    }

}
