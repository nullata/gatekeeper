package com.gatekeeper.services;

import com.gatekeeper.entity.ApiTokens;
import com.gatekeeper.repos.ApiTokensRepository;
import com.gatekeeper.validators.ValidationCompleteEvent;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 *
 * @author null
 */
@Service
public class KeyFetcherService {

    private final ApiTokensRepository apiTokensRepository;
    private boolean isValidationComplete = false;

    public KeyFetcherService(ApiTokensRepository apiTokensRepository) {
        this.apiTokensRepository = apiTokensRepository;
    }

    @EventListener
    public void onValidationComplete(ValidationCompleteEvent event) {
        this.isValidationComplete = true;
    }
    
    @Cacheable(value = "gakekeeper", key = "#requestKey")
    public boolean apiKeyValidator(String requestKey) {
        if (isValidationComplete) {
            Optional<ApiTokens> token = apiTokensRepository.findByUserTokens(requestKey);
            if (token.isPresent()) {
                return true;
            }
        }
        return false;
    }
}
